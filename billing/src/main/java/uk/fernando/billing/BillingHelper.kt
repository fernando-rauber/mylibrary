package uk.fernando.billing

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.android.billingclient.api.Purchase.PurchaseState.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.min

private enum class ProductState {
    PRODUCT_STATE_UN_PURCHASED, PRODUCT_STATE_PENDING, PRODUCT_STATE_PURCHASED, PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED
}

private const val RECONNECT_TIMER_START_MILLISECONDS = 1L * 1000L
private const val RECONNECT_TIMER_MAX_TIME_MILLISECONDS = 1000L * 60L * 10L

class BillingHelper private constructor(
    application: Application,
    private val defaultScope: CoroutineScope,
    knownInAppProducts: Array<String>? = null,
    knownSubscriptionProducts: Array<String>? = null,
    publicKey: String,
) : PurchasesUpdatedListener, BillingClientStateListener {

    private var billingClient: BillingClient

    private var knownInAppProducts: List<String>? // purchase only once
    private var knownSubscriptionProducts: List<String>?

    private val productStateMap: MutableMap<String, MutableStateFlow<ProductState>> = HashMap()
    private val productDetailsMap: MutableMap<String, MutableStateFlow<ProductDetails?>> = HashMap()

    // Time to try to reconnect to Google play
    private var reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS

    private val billingFlowInProcess = MutableStateFlow(false)
    private val billingState = MutableStateFlow<BillingState>(BillingState.Init)

    init {
        this.knownInAppProducts = knownInAppProducts?.toList() ?: emptyList()
        this.knownSubscriptionProducts = knownSubscriptionProducts?.toList() ?: emptyList()

        Security.setPublicKey(publicKey)

        //Add flow for in app purchases
        addProductFlows(this.knownInAppProducts)
        addProductFlows(this.knownSubscriptionProducts)

        billingClient = BillingClient.newBuilder(application)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        Log.d(TAG, "onBillingSetupFinished: $responseCode $debugMessage")

        when (responseCode) {
            OK -> {

                reconnectMilliseconds = RECONNECT_TIMER_START_MILLISECONDS
                // The billing client is ready. You can query purchases here.
                defaultScope.launch {
                    queryProductDetailsAsync()
                    restorePurchases()
                }
            }
            else -> retryBillingServiceConnection()
        }
    }

    /**
     * Called by initializeFlows to create the various Flow objects we're planning to emit.
     * @param productList a List<String> of Products representing purchases.
     */
    private fun addProductFlows(productList: List<String>?) {
        if (null == productList) {
            Log.e(TAG, "addProductFlows: ProductList is either null or empty.")
            return
        }
        for (product in productList) {
            val productState = MutableStateFlow(ProductState.PRODUCT_STATE_UN_PURCHASED)
            val details = MutableStateFlow<ProductDetails?>(null)
            details.subscriptionCount.map { count -> count > 0 }
                .distinctUntilChanged() // only react to true<->false changes
                .onEach { isActive -> // configure an action
                    if (isActive)
                        queryProductDetailsAsync()
                }
                .launchIn(defaultScope) // launch it inside defaultScope

            productStateMap[product] = productState
            productDetailsMap[product] = details
        }
    }

    /**
     * Calls the billing client functions to query product details for the in-app Products.
     * Product details are useful for displaying item names and price lists to the user, and are
     * required to make a purchase.
     */
    private fun queryProductDetailsAsync() {
        // One Time Purchase
        knownInAppProducts?.forEach { product ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            )

            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            ) { billingResult,
                productDetailsList ->

                onProductDetailsResponse(billingResult, productDetailsList)
            }
        }

        // Subscription
        knownSubscriptionProducts?.forEach { product ->
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(product)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            billingClient.queryProductDetailsAsync(
                QueryProductDetailsParams.newBuilder().setProductList(productList).build()
            ) { billingResult,
                productDetailsList ->

                onProductDetailsResponse(billingResult, productDetailsList)
            }
        }
    }

    /**
     * This calls all the products available and checks if they have been purchased.
     * You should call it every time the activity starts
     */
    private suspend fun restorePurchases() {
        var purchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        purchasesResult.billingResult.let { billingResult ->

            if (billingResult.responseCode != OK) {
                Log.e(TAG, "Problem getting purchases: " + billingResult.debugMessage)
            } else {
                processPurchase(purchasesResult.purchasesList)
            }
        }


        purchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )
        purchasesResult.billingResult.let { billingResult ->
            if (billingResult.responseCode != OK) {
                Log.e(TAG, "Problem getting purchases: " + billingResult.debugMessage)
            } else {
                processPurchase(purchasesResult.purchasesList)
            }
        }
    }

    private suspend fun getPurchases(products: Array<String>, productType: String): List<Purchase> {
        val purchasesResult = billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(productType)
                .build()
        )

        val br = purchasesResult.billingResult
        val returnPurchasesList: MutableList<Purchase> = LinkedList()
        if (br.responseCode != OK) {
            Log.e(TAG, "Problem getting purchases: " + br.debugMessage)
        } else {
            val purchasesList = purchasesResult.purchasesList
            for (purchase in purchasesList) {
                for (product in products) {
                    for (purchaseProduct in purchase.products) {
                        if (purchaseProduct == product) {
                            returnPurchasesList.add(purchase)
                        }
                    }
                }
            }
        }
        return returnPurchasesList
    }

    /**
     * This is called right after [queryProductDetailsAsync]. It gets all the products available
     * and get the details for all of them.
     */
    private fun onProductDetailsResponse(billingResult: BillingResult, productDetailsList: List<ProductDetails>) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        when (responseCode) {
            OK -> {
                Log.i(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
                if (productDetailsList.isEmpty()) {
                    Log.e(
                        TAG,
                        "onProductDetailsResponse: " +
                                "Found null or empty ProductDetails. " +
                                "Check to see if the Products you requested are correctly published " +
                                "in the Google Play Console."
                    )
                } else {
                    for (productDetails in productDetailsList) {
                        val product = productDetails.productId
                        val detailsMutableFlow = productDetailsMap[product]
                        detailsMutableFlow?.tryEmit(productDetails) ?: Log.e(TAG, "Unknown product: $product")
                    }
                }
            }
            SERVICE_UNAVAILABLE, BILLING_UNAVAILABLE, ITEM_UNAVAILABLE, DEVELOPER_ERROR, ERROR -> {
                billingState.tryEmit(BillingState.Error("onProductDetailsResponse: $responseCode $debugMessage"))
                Log.e(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }
            USER_CANCELED -> Log.i(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            FEATURE_NOT_SUPPORTED, ITEM_ALREADY_OWNED, ITEM_NOT_OWNED -> Log.wtf(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            else -> {
                billingState.tryEmit(BillingState.Crashlytics("onPurchasesUpdated responseCode: [$responseCode]: debugMessage: $debugMessage"))
                Log.wtf(TAG, "onProductDetailsResponse: $responseCode $debugMessage")
            }
        }
    }

    fun launchBillingFlow(activity: Activity?, product: String, vararg upgradeProductsVarargs: String) {
        val productDetails = productDetailsMap[product]?.value
        if (null != productDetails) {

            val productDetailsParamsList =
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )


            val billingFlowParamsBuilder = BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)

            val upgradeProducts = arrayOf(*upgradeProductsVarargs)
            defaultScope.launch {
                val heldSubscriptions = getPurchases(upgradeProducts, BillingClient.ProductType.INAPP)
                when (heldSubscriptions.size) {
                    1 -> {
                        val purchase = heldSubscriptions[0]

                        billingFlowParamsBuilder.setSubscriptionUpdateParams(
                            BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                                .setOldPurchaseToken(purchase.purchaseToken)
                                .build()
                        )
                    }
                    0 -> {
                    }
                    else -> Log.e(TAG, heldSubscriptions.size.toString() + " subscriptions subscribed to. Upgrade not possible.")
                }
                val br = billingClient.launchBillingFlow(
                    activity!!,
                    billingFlowParamsBuilder.build()
                )
                if (br.responseCode == OK) {
                    billingFlowInProcess.emit(true)
                } else {
                    Log.e(TAG, "Billing failed: + " + br.debugMessage)
                }
            }
        } else {
            Log.e(TAG, "ProductDetails not found for: $product")
        }
    }

    /**
     * This method is called right after [launchBillingFlow] which helps you complete the
     * purchase of a product
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, list: MutableList<Purchase>?) {
        when (billingResult.responseCode) {
            OK -> if (null != list) {
                processPurchase(list)
                return
            } else Log.d(TAG, "Null Purchase List Returned from OK response!")

            USER_CANCELED -> Log.i(TAG, "onPurchasesUpdated: User canceled the purchase")
            ITEM_ALREADY_OWNED -> billingState.tryEmit(BillingState.Success(""))
            SERVICE_DISCONNECTED, SERVICE_UNAVAILABLE, BILLING_UNAVAILABLE, ITEM_UNAVAILABLE, DEVELOPER_ERROR, ERROR -> {
                billingState.tryEmit(BillingState.Error("onPurchasesUpdated responseCode: [${billingResult.responseCode}]: debugMessage: ${billingResult.debugMessage}"))
                Log.e(TAG, "onPurchasesUpdated:")
            }
            else -> {
                billingState.tryEmit(BillingState.Crashlytics("onPurchasesUpdated responseCode: [${billingResult.responseCode}]: debugMessage: ${billingResult.debugMessage}"))
                Log.d(TAG, "BillingResult [" + billingResult.responseCode + "]: " + billingResult.debugMessage)
            }
        }
    }

    /**
     * This checks the purchase of a product and acknowledge it if the valid.
     * Notice that you should implement your own server to check the validity of
     * a purchase.
     */
    private fun processPurchase(purchases: List<Purchase>?) {
        val updatedProducts = HashSet<String>()
        if (null != purchases) {
            for (purchase in purchases) {
                for (product in purchase.products) {
                    val productStateFlow = productStateMap[product]
                    if (null == productStateFlow) {
                        Log.e(TAG, "Unknown Product: $product Check to make sure Product matches ProductS in the Play developer console.")
                        continue
                    }
                    updatedProducts.add(product)
                }
                // Global check to make sure all purchases are signed correctly.
                // This check is best performed on your server.
                val purchaseState = purchase.purchaseState
                if (purchaseState == PURCHASED) {
                    if (!isSignatureValid(purchase)) {
                        Log.e(TAG, "Invalid signature. Check to make sure your public key is correct.")
                        continue
                    }

                    // only set the purchased state after we've validated the signature.
                    setProductStateFromPurchase(purchase)

                    defaultScope.launch {
                        for (product in purchase.products) {
                            if (!purchase.isAcknowledged) {

                                // // Acknowledge item and change its state
                                val billingResult = billingClient.acknowledgePurchase(
                                    AcknowledgePurchaseParams.newBuilder()
                                        .setPurchaseToken(purchase.purchaseToken)
                                        .build()
                                )
                                if (billingResult.responseCode != OK) {
                                    Log.e(TAG, "Error acknowledging purchase: ${purchase.products}")
                                } else {
                                    // purchase acknowledged
                                    val productStateFlow = productStateMap[product]
                                    productStateFlow?.tryEmit(ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED)

                                    billingState.tryEmit(BillingState.Success(product))
                                }

                            }
                        }
                    }
                } else {
                    // make sure the state is set
                    setProductStateFromPurchase(purchase)
                }
            }
        } else {
            Log.d(TAG, "Empty purchase list.")
        }
    }

    /**
     * This sets the state of every product inside [productStateMap]
     */
    private fun setProductStateFromPurchase(purchase: Purchase) {
        if (purchase.products.isEmpty()) {
            Log.e(TAG, "Empty list of products")
            return
        }

        for (product in purchase.products) {
            val productState = productStateMap[product]
            if (null == productState) {
                Log.e(TAG, "Unknown Product $product Check to make sure Product matches ProductS in the Play developer console.")
                continue
            }
            Log.e(TAG, "****** ${purchase.purchaseState} ")
            when (purchase.purchaseState) {
                PENDING -> {

                    productState.tryEmit(ProductState.PRODUCT_STATE_PENDING)
                }
                UNSPECIFIED_STATE -> productState.tryEmit(ProductState.PRODUCT_STATE_UN_PURCHASED)
                PURCHASED -> {
                    if (purchase.isAcknowledged)
                        productState.tryEmit(ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED)
                    else
                        productState.tryEmit(ProductState.PRODUCT_STATE_PURCHASED)
                }

                else -> Log.e(TAG, "Purchase in unknown state: ${purchase.purchaseState}")
            }
        }
    }

    /**
     * @return Flow which says whether a purchase has been purchased and acknowledge
     */
    fun isPurchased(product: String): Flow<Boolean> {
        val productStateFLow = productStateMap[product]!!
        return productStateFLow.map { productState -> productState == ProductState.PRODUCT_STATE_PURCHASED_AND_ACKNOWLEDGED }
    }

    fun getProductPrice(product: String): Flow<String?> {
        val productDetailsFlow = productDetailsMap[product]!!
        return productDetailsFlow.mapNotNull { productDetails ->
            productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
        }
    }

    /**
     * This should check the validity of your purchase with a secure server
     * making this function unnecessary
     */
    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(purchase.originalJson, purchase.signature)
    }


    fun getBillingState() = billingState

    override fun onBillingServiceDisconnected() {
        Log.i(TAG, "Service disconnected")
        retryBillingServiceConnection()
    }

    private fun retryBillingServiceConnection() {
        handler.postDelayed({ billingClient.startConnection(this) }, reconnectMilliseconds)
        reconnectMilliseconds = min(reconnectMilliseconds * 2, RECONNECT_TIMER_MAX_TIME_MILLISECONDS)
    }

    companion object {
        private const val TAG = "BillingHelper"

        private val handler = Handler(Looper.getMainLooper())

        @Volatile
        private var sInstance: BillingHelper? = null

        @JvmStatic
        fun getInstance(application: Application, defaultScope: CoroutineScope, knownInAppProducts: Array<String>, knowConsumableInAppKUSs: Array<String>, publicKey: String) = sInstance ?: synchronized(this) {
            sInstance ?: BillingHelper(application, defaultScope, knownInAppProducts, knowConsumableInAppKUSs, publicKey)
                .also { sInstance = it }
        }

    }
}
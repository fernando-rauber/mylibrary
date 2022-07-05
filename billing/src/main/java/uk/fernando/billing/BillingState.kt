package uk.fernando.billing

sealed class BillingState {

    object Init : BillingState()
    class Error(val message: String) : BillingState()
    class Crashlytics(val message: String) : BillingState()
    class Success(val product: String) : BillingState()
}

# **Version 1.4.4**


# Setup
### settings.gradle
maven { url 'https://jitpack.io' }

![image](https://user-images.githubusercontent.com/6031955/184131299-a917682a-1922-44c6-b292-529107e49be6.png)

##

# Lib: Utilities

### build.gradle

> implementation 'com.github.fernando-rauber.mylibrary:util:version'

### Uses

These are the utilities for this lib:
Components:
- MyDialog(shape: Shape = MaterialTheme.shapes.medium, content: @Composable () -> Unit)
- UpdateStatusBar(color: Color)
- MyAnimatedVisibility(visible: Boolean, content: @Composable () -> Unit)
- MyButton(...)
- MyIconButton(...)
- OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit)

Extentions:
- Date.formatToTime(format: String)
- Date.formatToDate(format: String)

- Modifier.clickableSingle(enabled: Boolean = true, ripple: Bollean, onClickLabel: String? = null, role: Role? = null, onClick: () -> Unit)

Gereral:
- TAG
- NavController.safeNav(direction: String)
- Context.vibrate()
- MediaPlayer.playAudio(enableSound: Boolean)
- Context.isNetworkAvailable(): Boolean

# Lib: Advertising with AdMob

### Create AdMob Account
https://admob.google.com/home/get-started/

Create a new project in AdMob and create Ad Units.

### build.gradle

> implementation 'com.github.fernando-rauber.mylibrary:advertising:version'

### Initialize AdMob -> Application class

> MyAdvertising.initialize(this)

### manifest.xml
Inside application tag add:
```
< meta-data
            android:name="com.google.android.gms.ads.APPLICATION_ID"
            android:value="ca-app-pub-345435435345345~345345345345345345" />
```            
ID found in your AdMob account -> your app -> App Settings -> App Id

## Using Ads

### Banner
```
 AdBanner(
            modifier = Modifier,
            unitId = stringResource(R.string.ad_banner)
        )
```
### Interstitial
```
 val interstitialAd = AdInterstitial(LocalContext.current as MainActivity, stringResource(R.string.ad_full_page))
 interstitialAd.showAdvert()
``` 
 Interestitial Ads needs a few second to load before display
 
 If you want to listen to the state of the interestitial Ad, the function:
 showAdvert() returns the state of the add: LOADING, FAIL, LOADED, DISMISSED and OPENED
 
 ## Additional info
 To display the ads on your physical device, please add this line:
 ```
 MyAdvertising.setDeviceID(listOf("your device id"))
 MyAdvertising.initialize(this)
 ```  

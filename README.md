## GoogleMaps API
Example using Google Maps Api

#### How to start?
You need to get [API Key](https://console.developers.google.com/flows/enableapi?apiid=maps_android_backend)
Google Maps and enable:

* Google Places API for Android
* Google Maps Directions API
* Google Maps Distance Matrix API

Create /res/value/google_maps_api.xml and add API key
```
<resources>
    <string name="google_maps_api_key"
        templateMergeStrategy="preserve"
        translatable="false">AIzaSyBwA7************QHt5KWfevN</string>
</resources>
```

For securing safety API key add *google_maps_api.xml* to *.gitignore*

That's all you need.
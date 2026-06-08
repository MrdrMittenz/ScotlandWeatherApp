package com.scotlandweather.app.data.model

data class ScotlandLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String = "Scotland",
    val region: String = "",
    val isCustom: Boolean = false
) {
    companion object {
        val ALL: List<ScotlandLocation> = listOf(
            ScotlandLocation("Aberdeen", 57.148, -2.094),
            ScotlandLocation("Ayr", 55.458, -4.629),
            ScotlandLocation("Dundee", 56.462, -2.971),
            ScotlandLocation("Edinburgh", 55.953, -3.189),
            ScotlandLocation("Glasgow", 55.864, -4.252),
            ScotlandLocation("Inverness", 57.477, -4.225),
            ScotlandLocation("Perth", 56.395, -3.434),
            ScotlandLocation("Stirling", 56.117, -3.940),
            ScotlandLocation("Oban", 56.415, -5.472),
            ScotlandLocation("Fort William", 56.820, -5.105),
            ScotlandLocation("Ullapool", 57.899, -5.159),
            ScotlandLocation("Thurso", 58.593, -3.522),
            ScotlandLocation("Wick", 58.440, -3.089),
            ScotlandLocation("Kirkwall", 58.982, -2.959),
            ScotlandLocation("Stornoway", 58.209, -6.386),
            ScotlandLocation("Portree", 57.413, -6.194),
            ScotlandLocation("Mallaig", 57.006, -5.831),
            ScotlandLocation("Campbeltown", 55.426, -5.606),
            ScotlandLocation("Dumfries", 55.072, -3.605),
            ScotlandLocation("Stranraer", 54.904, -5.027),
            ScotlandLocation("Lerwick", 60.155, -1.145),
            ScotlandLocation("Isle of Lewis", 58.212, -6.619)
        )
    }
}

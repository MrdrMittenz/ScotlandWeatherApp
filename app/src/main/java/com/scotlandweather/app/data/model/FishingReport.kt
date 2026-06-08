package com.scotlandweather.app.data.model

data class FishingSpecies(
    val name: String,
    val seaonStart: Int,
    val seasonEnd: Int,
    val bestTempLow: Double,
    val bestTempHigh: Double,
    val waterType: String,
    val description: String
) {
    fun isInSeason(month: Int): Boolean {
        if (seaonStart <= seasonEnd) return month in seaonStart..seasonEnd
        return month >= seaonStart || month <= seasonEnd
    }

    fun tempScore(temp: Double): Double {
        if (temp in bestTempLow..bestTempHigh) return 1.0
        val mid = (bestTempLow + bestTempHigh) / 2.0
        val dist = kotlin.math.abs(temp - mid)
        return (1.0 - dist / 20.0).coerceIn(0.0, 1.0)
    }
}

data class CatchReport(
    val species: String,
    val location: String,
    val date: String,
    val source: String,
    val snippet: String,
    val lat: Double,
    val lon: Double
)

object ScottishSpecies {
    val all: List<FishingSpecies> = listOf(
        FishingSpecies("Mackerel", 6, 9, 10.0, 18.0, "Sea", "Summer visitor, arrives when sea temps reach 10°C"),
        FishingSpecies("Sea Trout", 5, 9, 8.0, 16.0, "Sea", "Sea-run brown trout, feeds in estuaries"),
        FishingSpecies("Sea Bass", 6, 10, 12.0, 20.0, "Sea", "Warm water predator, found near structure"),
        FishingSpecies("Cod", 10, 3, 4.0, 12.0, "Sea", "Winter species, prefers cold water"),
        FishingSpecies("Pollock", 1, 12, 6.0, 16.0, "Sea", "Year-round, found near reefs and wrecks"),
        FishingSpecies("Wrasse", 5, 10, 8.0, 18.0, "Sea", "Summer species, found in rocky ground"),
        FishingSpecies("Haddock", 1, 12, 4.0, 12.0, "Sea", "Deep water, sandy seabed"),
        FishingSpecies("Skate", 1, 12, 6.0, 14.0, "Sea", "Deep water, catch and release"),
        FishingSpecies("Conger Eel", 6, 10, 10.0, 18.0, "Sea", "Night feeder, found in wrecks and reefs"),
        FishingSpecies("Lobster", 1, 12, 6.0, 16.0, "Sea", "Creel caught, rocky ground")
    )
}

object SeaLochs {
    val all: List<ScotlandLocation> = listOf(
        ScotlandLocation("Loch Fyne", 55.85, -5.35, "Scotland", "Argyll"),
        ScotlandLocation("Loch Long", 56.05, -4.85, "Scotland", "Argyll"),
        ScotlandLocation("Loch Goil", 56.15, -4.90, "Scotland", "Argyll"),
        ScotlandLocation("Loch Etive", 56.45, -5.25, "Scotland", "Argyll"),
        ScotlandLocation("Loch Creran", 56.55, -5.35, "Scotland", "Argyll"),
        ScotlandLocation("Loch Linnhe", 56.65, -5.35, "Scotland", "Highland"),
        ScotlandLocation("Loch Sunart", 56.70, -5.70, "Scotland", "Highland"),
        ScotlandLocation("Loch Hourn", 57.10, -5.50, "Scotland", "Highland"),
        ScotlandLocation("Loch Nevis", 57.00, -5.65, "Scotland", "Highland"),
        ScotlandLocation("Loch Broom", 57.85, -5.10, "Scotland", "Highland"),
        ScotlandLocation("Loch Ewe", 57.80, -5.60, "Scotland", "Highland"),
        ScotlandLocation("Loch Torridon", 57.55, -5.55, "Scotland", "Highland"),
        ScotlandLocation("Loch Carron", 57.40, -5.45, "Scotland", "Highland"),
        ScotlandLocation("Loch Kishorn", 57.40, -5.60, "Scotland", "Highland"),
        ScotlandLocation("Loch Alsh", 57.30, -5.60, "Scotland", "Highland"),
        ScotlandLocation("Loch Duich", 57.25, -5.50, "Scotland", "Highland"),
        ScotlandLocation("Loch Sween", 55.95, -5.60, "Scotland", "Argyll"),
        ScotlandLocation("Loch Melfort", 56.25, -5.50, "Scotland", "Argyll"),
        ScotlandLocation("Loch Feochan", 56.35, -5.45, "Scotland", "Argyll"),
        ScotlandLocation("Loch Ailort", 56.85, -5.70, "Scotland", "Highland"),
        ScotlandLocation("Loch Moidart", 56.80, -5.75, "Scotland", "Highland"),
        ScotlandLocation("Loch Shiel", 56.80, -5.55, "Scotland", "Highland"),
        ScotlandLocation("Loch Glencoul", 58.25, -5.00, "Scotland", "Highland"),
        ScotlandLocation("Loch Laxford", 58.35, -5.05, "Scotland", "Highland"),
        ScotlandLocation("Loch Inchard", 58.45, -5.10, "Scotland", "Highland"),
        ScotlandLocation("Loch Eriboll", 58.50, -4.65, "Scotland", "Highland"),
        ScotlandLocation("Loch Hope", 58.50, -4.55, "Scotland", "Highland"),
        // Major freshwater lochs
        ScotlandLocation("Loch Lomond", 56.15, -4.65, "Scotland", "Stirling"),
        ScotlandLocation("Loch Ness", 57.25, -4.45, "Scotland", "Highland"),
        ScotlandLocation("Loch Awe", 56.35, -5.20, "Scotland", "Argyll"),
        ScotlandLocation("Loch Tay", 56.50, -4.15, "Scotland", "Perthshire"),
        ScotlandLocation("Loch Maree", 57.70, -5.50, "Scotland", "Highland"),
        ScotlandLocation("Loch Shin", 58.10, -4.55, "Scotland", "Highland"),
        ScotlandLocation("Loch Rannoch", 56.70, -4.30, "Scotland", "Perthshire")
    )
}

data class PortWebcam(
    val name: String,
    val region: String,
    val lat: Double,
    val lon: Double,
    val imageUrl: String,
    val source: String = ""
)

object ScottishPortWebcams {
    val all: List<PortWebcam> = listOf(
        PortWebcam("Kirkwall Harbour", "Orkney", 58.982, -2.959,
            "https://imgproxy.windy.com/_/preview/plain/current/1544173823/original.jpg", "Windy.com"),
        PortWebcam("Aberdeen Harbour", "Aberdeenshire", 57.148, -2.094,
            "https://www.worldcam.pl/images/webcams/420x236/aberdeen-port-preview.jpg", "WorldCam"),
        PortWebcam("Oban Bay", "Argyll", 56.415, -5.472,
            "https://imgproxy.windy.com/_/full/plain/current/1701007994/original.jpg", "Windy.com"),
        PortWebcam("Armadale Ferry Terminal", "Isle of Skye", 57.073, -5.886,
            "https://imgproxy.windy.com/_/full/plain/current/1685712074/original.jpg", "Windy.com"),
        PortWebcam("Brodick Ferry Terminal", "Isle of Arran", 55.577, -5.146,
            "https://imgproxy.windy.com/_/full/plain/current/1737701123/original.jpg", "Windy.com")
    )
}

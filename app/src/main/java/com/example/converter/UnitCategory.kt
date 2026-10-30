package com.example.converter

data class ConversionUnit(
    val name: String,
    val symbol: String,
    // Base conversion: factor to standard SI base unit (or null if non-linear like temperature/fuel)
    val toBaseFactor: Double = 1.0
)

enum class UnitCategory(
    val displayName: String,
    val units: List<ConversionUnit>
) {
    LENGTH(
        "Length",
        listOf(
            ConversionUnit("Meters", "m", 1.0),
            ConversionUnit("Kilometers", "km", 1000.0),
            ConversionUnit("Centimeters", "cm", 0.01),
            ConversionUnit("Millimeters", "mm", 0.001),
            ConversionUnit("Inches", "in", 0.0254),
            ConversionUnit("Feet", "ft", 0.3048),
            ConversionUnit("Yards", "yd", 0.9144),
            ConversionUnit("Miles", "mi", 1609.344),
            ConversionUnit("Nautical Miles", "NM", 1852.0)
        )
    ),
    MASS(
        "Mass",
        listOf(
            ConversionUnit("Kilograms", "kg", 1.0),
            ConversionUnit("Grams", "g", 0.001),
            ConversionUnit("Milligrams", "mg", 0.000001),
            ConversionUnit("Pounds", "lb", 0.45359237),
            ConversionUnit("Ounces", "oz", 0.028349523),
            ConversionUnit("Metric Tons", "t", 1000.0),
            ConversionUnit("Stones", "st", 6.35029318)
        )
    ),
    TEMPERATURE(
        "Temperature",
        listOf(
            ConversionUnit("Celsius", "°C"),
            ConversionUnit("Fahrenheit", "°F"),
            ConversionUnit("Kelvin", "K"),
            ConversionUnit("Rankine", "°R")
        )
    ),
    AREA(
        "Area",
        listOf(
            ConversionUnit("Square Meters", "m²", 1.0),
            ConversionUnit("Square Kilometers", "km²", 1e6),
            ConversionUnit("Square Feet", "ft²", 0.09290304),
            ConversionUnit("Square Inches", "in²", 0.00064516),
            ConversionUnit("Acres", "ac", 4046.8564224),
            ConversionUnit("Hectares", "ha", 10000.0)
        )
    ),
    VOLUME(
        "Volume",
        listOf(
            ConversionUnit("Liters", "L", 1.0),
            ConversionUnit("Milliliters", "mL", 0.001),
            ConversionUnit("Cubic Meters", "m³", 1000.0),
            ConversionUnit("Gallons (US)", "gal", 3.785411784),
            ConversionUnit("Quarts (US)", "qt", 0.946352946),
            ConversionUnit("Pints (US)", "pt", 0.473176473),
            ConversionUnit("Cups (US)", "cup", 0.2365882365),
            ConversionUnit("Fluid Ounces (US)", "fl oz", 0.02957352956)
        )
    ),
    SPEED(
        "Speed",
        listOf(
            ConversionUnit("Meters/Second", "m/s", 1.0),
            ConversionUnit("Kilometers/Hour", "km/h", 1.0 / 3.6),
            ConversionUnit("Miles/Hour", "mph", 0.44704),
            ConversionUnit("Knots", "kn", 0.514444),
            ConversionUnit("Feet/Second", "ft/s", 0.3048)
        )
    ),
    TIME(
        "Time",
        listOf(
            ConversionUnit("Seconds", "s", 1.0),
            ConversionUnit("Milliseconds", "ms", 0.001),
            ConversionUnit("Minutes", "min", 60.0),
            ConversionUnit("Hours", "hr", 3600.0),
            ConversionUnit("Days", "d", 86400.0),
            ConversionUnit("Weeks", "wk", 604800.0),
            ConversionUnit("Years (365d)", "yr", 31536000.0)
        )
    ),
    DIGITAL(
        "Digital Storage",
        listOf(
            ConversionUnit("Bytes", "B", 1.0),
            ConversionUnit("Kilobytes", "KB", 1024.0),
            ConversionUnit("Megabytes", "MB", 1024.0 * 1024.0),
            ConversionUnit("Gigabytes", "GB", 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("Terabytes", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("Petabytes", "PB", 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0)
        )
    ),
    PRESSURE(
        "Pressure",
        listOf(
            ConversionUnit("Pascals", "Pa", 1.0),
            ConversionUnit("Kilopascals", "kPa", 1000.0),
            ConversionUnit("Bar", "bar", 100000.0),
            ConversionUnit("Pounds/Sq Inch", "psi", 6894.757),
            ConversionUnit("Standard Atmospheres", "atm", 101325.0),
            ConversionUnit("Torr (mmHg)", "mmHg", 133.322368)
        )
    ),
    ENERGY(
        "Energy",
        listOf(
            ConversionUnit("Joules", "J", 1.0),
            ConversionUnit("Kilojoules", "kJ", 1000.0),
            ConversionUnit("Calories", "cal", 4.184),
            ConversionUnit("Kilocalories (dietary)", "kcal", 4184.0),
            ConversionUnit("Watt-hours", "Wh", 3600.0),
            ConversionUnit("Kilowatt-hours", "kWh", 3.6e6),
            ConversionUnit("British Thermal Units", "BTU", 1055.056)
        )
    ),
    ANGLE(
        "Angle",
        listOf(
            ConversionUnit("Degrees", "°", 1.0),
            ConversionUnit("Radians", "rad", 180.0 / Math.PI),
            ConversionUnit("Gradians", "grad", 0.9),
            ConversionUnit("Arcminutes", "arcmin", 1.0 / 60.0),
            ConversionUnit("Arcseconds", "arcsec", 1.0 / 3600.0)
        )
    )
}

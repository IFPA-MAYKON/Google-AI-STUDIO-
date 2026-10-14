package com.example.calculator.converter

enum class UnitCategory(val displayName: String) {
    LENGTH("Comprimento"),
    MASS("Massa / Peso"),
    TEMPERATURE("Temperatura"),
    AREA("Área"),
    VOLUME("Volume"),
    DATA("Armazenamento"),
    SPEED("Velocidade")
}

data class ConversionUnit(
    val name: String,
    val symbol: String,
    val factorToBase: Double // Base is meter, gram, etc.
)

object UnitConverterData {
    val categories: Map<UnitCategory, List<ConversionUnit>> = mapOf(
        UnitCategory.LENGTH to listOf(
            ConversionUnit("Quilômetro", "km", 1000.0),
            ConversionUnit("Metro", "m", 1.0),
            ConversionUnit("Centímetro", "cm", 0.01),
            ConversionUnit("Milímetro", "mm", 0.001),
            ConversionUnit("Milha", "mi", 1609.344),
            ConversionUnit("Jarda", "yd", 0.9144),
            ConversionUnit("Pé", "ft", 0.3048),
            ConversionUnit("Polegada", "in", 0.0254)
        ),
        UnitCategory.MASS to listOf(
            ConversionUnit("Tonelada", "t", 1000000.0),
            ConversionUnit("Quilograma", "kg", 1000.0),
            ConversionUnit("Grama", "g", 1.0),
            ConversionUnit("Miligrama", "mg", 0.001),
            ConversionUnit("Libra", "lb", 453.59237),
            ConversionUnit("Onça", "oz", 28.3495)
        ),
        UnitCategory.TEMPERATURE to listOf(
            ConversionUnit("Celsius", "°C", 1.0),
            ConversionUnit("Fahrenheit", "°F", 1.0),
            ConversionUnit("Kelvin", "K", 1.0)
        ),
        UnitCategory.AREA to listOf(
            ConversionUnit("Quilômetro quadrado", "km²", 1000000.0),
            ConversionUnit("Metro quadrado", "m²", 1.0),
            ConversionUnit("Centímetro quadrado", "cm²", 0.0001),
            ConversionUnit("Hectare", "ha", 10000.0),
            ConversionUnit("Acre", "ac", 4046.86),
            ConversionUnit("Pé quadrado", "ft²", 0.092903)
        ),
        UnitCategory.VOLUME to listOf(
            ConversionUnit("Metro cúbico", "m³", 1000.0),
            ConversionUnit("Litro", "L", 1.0),
            ConversionUnit("Mililitro", "mL", 0.001),
            ConversionUnit("Galão (US)", "gal", 3.78541),
            ConversionUnit("Xícara", "xícara", 0.24)
        ),
        UnitCategory.DATA to listOf(
            ConversionUnit("Byte", "B", 1.0),
            ConversionUnit("Quilobyte", "KB", 1024.0),
            ConversionUnit("Megabyte", "MB", 1024.0 * 1024.0),
            ConversionUnit("Gigabyte", "GB", 1024.0 * 1024.0 * 1024.0),
            ConversionUnit("Terabyte", "TB", 1024.0 * 1024.0 * 1024.0 * 1024.0)
        ),
        UnitCategory.SPEED to listOf(
            ConversionUnit("Metros por segundo", "m/s", 1.0),
            ConversionUnit("Quilômetros por hora", "km/h", 1.0 / 3.6),
            ConversionUnit("Milhas por hora", "mph", 0.44704),
            ConversionUnit("Nós", "kn", 0.514444)
        )
    )

    fun convert(value: Double, from: ConversionUnit, to: ConversionUnit, category: UnitCategory): Double {
        if (category == UnitCategory.TEMPERATURE) {
            // Temperature special conversions
            val celsius = when (from.symbol) {
                "°C" -> value
                "°F" -> (value - 32.0) * 5.0 / 9.0
                "K" -> value - 273.15
                else -> value
            }
            return when (to.symbol) {
                "°C" -> celsius
                "°F" -> (celsius * 9.0 / 5.0) + 32.0
                "K" -> celsius + 273.15
                else -> celsius
            }
        }

        // Standard linear conversion: value * fromFactor / toFactor
        val inBase = value * from.factorToBase
        return inBase / to.factorToBase
    }
}

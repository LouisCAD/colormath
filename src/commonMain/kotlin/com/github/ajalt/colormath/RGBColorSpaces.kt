package com.github.ajalt.colormath

import com.github.ajalt.colormath.internal.*

object RGBColorSpaces {
    /**
     * sRGB color space
     *
     * ### Specifications
     * - [IEC 61966-2-1](https://webstore.iec.ch/publication/6169)
     */
    val SRGB: RGBColorSpace = com.github.ajalt.colormath.SRGB

    /**
     * Linear sRGB color space
     *
     * ### Specifications
     * - [IEC 61966-2-1](https://webstore.iec.ch/publication/6169)
     */
    val LINEAR_SRGB: RGBColorSpace = RGBColorSpace(
        "Linear sRGB",
        WhitePoint.D65,
        RGBColorSpace.LinearTransferFunctions,
        SRGB_R,
        SRGB_G,
        SRGB_B,
    )

    /**
     * Adobe RGB 1998 color space
     *
     * ### Specifications
     * - [Adobe RGB (1998) Color Image Encoding](https://www.adobe.com/digitalimag/pdfs/AdobeRGB1998.pdf)
     */
    val ADOBE_RGB: RGBColorSpace = RGBColorSpace(
        "Adobe RGB",
        WhitePoint.D65,
        RGBColorSpace.GammaTransferFunctions(2f * 51f / 256f),
        Chromaticity.from_xy(0.64, 0.33),
        Chromaticity.from_xy(0.21, 0.71),
        Chromaticity.from_xy(0.15, 0.06),
    )

    /**
     * ITU-R Recommendation BT.2020 color space, also known as BT.2020 or REC.2020
     *
     * ### Specifications
     * - [ITU-R  BT.2020-2](https://www.itu.int/dms_pubrec/itu-r/rec/bt/R-REC-BT.2020-2-201510-I!!PDF-E.pdf)
     */
    val BT_2020: RGBColorSpace = RGBColorSpace(
        "BT.2020",
        WhitePoint.D65,
        RGBColorSpace.StandardTransferFunctions(
            a = 1 / 1.0993,
            b = 0.0993 / 1.0993,
            c = 1 / 4.5,
            d = 0.0181 / (1 / 4.5),
            e = 0.0,
            f = 0.0,
            gamma = 1 / 0.45
        ),
        Chromaticity.from_xy(0.708, 0.292),
        Chromaticity.from_xy(0.170, 0.797),
        Chromaticity.from_xy(0.131, 0.046),
    )

    /**
     * DCI P3 color space
     *
     * ### Specifications
     * - [RP 431-2:2011](https://ieeexplore.ieee.org/document/7290729)
     * - [Digital Cinema System Specification - Version 1.1](https://www.dcimovies.com/archives/spec_v1_1/DCI_DCinema_System_Spec_v1_1.pdf)
     */
    val DCI_P3: RGBColorSpace = RGBColorSpace(
        "DCI P3",
        WhitePoint("DCI P3", Chromaticity(0.314f, 0.351f)),
        RGBColorSpace.GammaTransferFunctions(2.6),
        Chromaticity.from_xy(0.680, 0.320),
        Chromaticity.from_xy(0.265, 0.690),
        Chromaticity.from_xy(0.150, 0.060),
    )

    /**
     * Display P3 color space
     *
     * ### Specifications
     * - [Apple](https://developer.apple.com/documentation/coregraphics/cgcolorspace/1408916-displayp3)
     * - [RP 431-2:2011](https://ieeexplore.ieee.org/document/7290729)
     * - [Digital Cinema System Specification - Version 1.1](https://www.dcimovies.com/archives/spec_v1_1/DCI_DCinema_System_Spec_v1_1.pdf)
     */
    val DISPLAY_P3: RGBColorSpace = RGBColorSpace(
        "Display P3",
        WhitePoint.D65,
        SRGB_TRANSFER_FUNCTIONS,
        Chromaticity.from_xy(0.680, 0.320),
        Chromaticity.from_xy(0.265, 0.690),
        Chromaticity.from_xy(0.150, 0.060),
    )

    /**
     * ROMM RGB color space, also known as ProPhoto RGB
     *
     * ### Specifications
     * - [ANSI/I3A IT10.7666:2003](https://www.color.org/ROMMRGB.pdf)
     */
    val ROMM_RGB: RGBColorSpace = RGBColorSpace(
        "ROMM RGB",
        WhitePoint.D50,
        RGBColorSpace.StandardTransferFunctions(1.0, 0.0, 1 / 16.0, 16 * 0.001953, 0.0, 0.0, 1.8),
        Chromaticity.from_xy(0.7347, 0.2653),
        Chromaticity.from_xy(0.1596, 0.8404),
        Chromaticity.from_xy(0.0366, 0.0001),
    )
}

/**
 * Create a new [RGBColorSpace] implementation with the given [name], [whitePoint], [transferFunctions], and [r][r]
 * [g][g] [b][b] primaries.
 */
fun RGBColorSpace(
    name: String,
    whitePoint: WhitePoint,
    transferFunctions: RGBColorSpace.TransferFunctions,
    r: Chromaticity,
    g: Chromaticity,
    b: Chromaticity,
): RGBColorSpace = RGBColorSpaceImpl(name, whitePoint, transferFunctions, r, g, b)

/**
 * The sRGB color space defined in [IEC 61966-2-1](https://webstore.iec.ch/publication/6169)
 */
object SRGB : RGBColorSpace {
    override val components: List<ColorComponentInfo> = rectangularComponentInfo("RGB")
    override operator fun invoke(r: Float, g: Float, b: Float, alpha: Float): RGB = RGB(r, g, b, alpha, this)
    override fun convert(color: Color): RGB = color.toSRGB()
    override fun create(components: FloatArray): RGB = doCreate(components, ::invoke)

    override val name: String = "sRGB"
    override val whitePoint: WhitePoint = WhitePoint.D65
    override val transferFunctions: RGBColorSpace.TransferFunctions = SRGB_TRANSFER_FUNCTIONS
    override val matrixToXyz: FloatArray = rgbToXyzMatrix(whitePoint, SRGB_R, SRGB_G, SRGB_B).rowMajor
    override val matrixFromXyz: FloatArray = Matrix(matrixToXyz).inverse().rowMajor
    override fun toString(): String = name
}

private data class RGBColorSpaceImpl(
    override val name: String,
    override val whitePoint: WhitePoint,
    override val transferFunctions: RGBColorSpace.TransferFunctions,
    private val r: Chromaticity,
    private val g: Chromaticity,
    private val b: Chromaticity,
) : RGBColorSpace {
    override val components: List<ColorComponentInfo> = rectangularComponentInfo("RGB")
    override operator fun invoke(r: Float, g: Float, b: Float, alpha: Float): RGB = RGB(r, g, b, alpha, this)
    override fun convert(color: Color): RGB = if (color is RGB) color.convertTo(this) else color.toXYZ().toRGB(this)
    override fun create(components: FloatArray): RGB = doCreate(components, ::invoke)

    override val matrixToXyz: FloatArray = rgbToXyzMatrix(whitePoint, r, g, b).rowMajor
    override val matrixFromXyz: FloatArray = Matrix(matrixToXyz).inverse().rowMajor
    override fun toString(): String = name
}

private val SRGB_R = Chromaticity.from_xy(0.640f, 0.330f)
private val SRGB_G = Chromaticity.from_xy(0.300f, 0.600f)
private val SRGB_B = Chromaticity.from_xy(0.150f, 0.060f)
private val SRGB_TRANSFER_FUNCTIONS =
    RGBColorSpace.StandardTransferFunctions(1 / 1.055f, 0.055f / 1.055f, 1 / 12.92f, 0.04045f, 0f, 0f, 2.4f)


// http://www.brucelindbloom.com/Eqn_RGB_XYZ_Matrix.html
private fun rgbToXyzMatrix(whitePoint: WhitePoint, r: Chromaticity, g: Chromaticity, b: Chromaticity): Matrix {
    val m = Matrix(
        r.x, g.x, b.x,
        r.y, g.y, b.y,
        r.z, g.z, b.z,
    ).inverse(inPlace = true)
    m.times(whitePoint.chromaticity.x, whitePoint.chromaticity.y, whitePoint.chromaticity.z) { Sr, Sg, Sb ->
        m[0, 0] = Sr * r.x
        m[1, 0] = Sg * g.x
        m[2, 0] = Sb * b.x

        m[0, 1] = Sr * r.y
        m[1, 1] = Sg * g.y
        m[2, 1] = Sb * b.y

        m[0, 2] = Sr * r.z
        m[1, 2] = Sg * g.z
        m[2, 2] = Sb * b.z
    }
    return m
}
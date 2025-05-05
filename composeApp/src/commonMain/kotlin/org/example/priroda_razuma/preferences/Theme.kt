package org.example.priroda_razuma.preferences

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import prirodarazumamobile.composeapp.generated.resources.Res
import prirodarazumamobile.composeapp.generated.resources.nunito
import prirodarazumamobile.composeapp.generated.resources.roboto_flex

object Theme {
    val colors = Colors()
    val fonts = Fonts()

    class Colors {
        val primary = Color(0xFFA3F49F)
        val secondary = Color(0xFFD3E29F)
        val background = Color.White
    }

    class Fonts {
        val nunito: FontFamily
            @Composable
            get() = FontFamily(Font(resource =  Res.font.nunito))

        val robotoFlex: FontFamily
            @Composable
            get() = FontFamily(Font(resource =  Res.font.roboto_flex))
    }
}
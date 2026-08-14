package com.adolfogonzalez.mareasihmpro.wear

import androidx.wear.watchface.complications.data.ComplicationData
import androidx.wear.watchface.complications.data.ComplicationType
import androidx.wear.watchface.complications.data.PlainComplicationText
import androidx.wear.watchface.complications.data.ShortTextComplicationData
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.adolfogonzalez.mareasihmpro.widget.SurfaceCache

class TideComplicationService : ComplicationDataSourceService() {
    override fun getPreviewData(type: ComplicationType): ComplicationData? =
        if (type == ComplicationType.SHORT_TEXT) short("1.2 m") else null

    override fun onComplicationRequest(request: ComplicationRequest, listener: ComplicationRequestListener) {
        val data = SurfaceCache.read(this)
        listener.onComplicationData(short("%.1f m".format(data.height)))
    }

    private fun short(value: String) = ShortTextComplicationData.Builder(
        text = PlainComplicationText.Builder(value).build(),
        contentDescription = PlainComplicationText.Builder("Altura de marea").build()
    ).build()
}

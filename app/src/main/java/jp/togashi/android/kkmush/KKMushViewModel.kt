package jp.togashi.android.kkmush

import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import timber.log.Timber

class KKMushViewModel(private val context: Context): BaseObservable() {
    @Bindable
    var source: String? = null
        set(value) {
            field = value
            converted = KKConverter.convert(value ?: "", koikeLevel + 1).also {
                _lastResult = it
            }.output
            notifyPropertyChanged(BR.source)
        }
    @Bindable
    var converted: String? = null
        set(value) {
            field = value
            notifyPropertyChanged(BR.converted)
        }
    @Bindable
    var koikeLevel: Int = 1
        set(value) {
            Timber.d("koikeLevel: $value")
            field = value
            converted = KKConverter.convert(source ?: "", koikeLevel + 1).also {
                _lastResult = it
            }.output
            notifyPropertyChanged(BR.koikeLevel)
            notifyPropertyChanged(BR.koikeLevelText)
        }
    @Bindable
    val koikeLevelMax = 3
    @get:Bindable
    val koikeLevelText: String
        get() = context.getString(R.string.koikelevel, koikeLevel + 1)

    private var _lastResult: KKConverter.ConvertResult? = null
    val lastResult: KKConverter.ConvertResult?
        get() = _lastResult
}

package ui.common.extensions

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.example.photoalbum.R
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso

fun View.snack(message: String) = Snackbar.make(this, message, Snackbar.LENGTH_LONG).show()

fun ImageView.load(link: String) = Picasso.with(context).load(link).error(R.drawable.baseline_image_24).into(this)

fun View.beVisible() { visibility = View.VISIBLE }
fun View.beInvisible()  { visibility = View.INVISIBLE }
fun View.beGone()  { visibility = View.GONE }
fun View.beInvisibleIf(shouldBeInVisible: Boolean) { visibility = if(shouldBeInVisible) View.INVISIBLE else View.VISIBLE }
fun View.beVisibleIf(shouldBeVisible: Boolean) { visibility = if(shouldBeVisible) View.VISIBLE else View.GONE }
fun View.beGoneIf(shouldBeGone: Boolean) = beVisibleIf(!shouldBeGone)

@ColorInt
fun Context.getColorFromAttr(
    @AttrRes attrColor: Int,
    typedValue: TypedValue = TypedValue(),
    resolveRefs: Boolean = true
): Int {
    theme.resolveAttribute(attrColor, typedValue, resolveRefs)
    return typedValue.data
}

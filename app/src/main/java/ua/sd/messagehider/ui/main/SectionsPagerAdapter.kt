package ua.sd.messagehider.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import ua.sd.messagehider.R

val TAB_TITLES = arrayOf(
    R.string.tab_text_1,
    R.string.tab_text_2
)

/**
 * A [FragmentStateAdapter] to page through a small, fixed number of fragments.
 */
class SectionsPagerAdapter(fa: FragmentActivity) :
    FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        // createFragment always supplies a new fragment instance each time the function is called.
        when (position) {
            0 -> return TextMessageFragment()
        }

        return ImageMessageFragment()
    }

    override fun getItemCount(): Int {
        return TAB_TITLES.size
    }
}
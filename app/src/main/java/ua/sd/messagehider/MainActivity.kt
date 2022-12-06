package ua.sd.messagehider

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import ua.sd.messagehider.databinding.ActivityMainBinding
import ua.sd.messagehider.ui.main.SectionsPagerAdapter
import ua.sd.messagehider.ui.main.TAB_TITLES

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val sectionsPagerAdapter = SectionsPagerAdapter(this)
        val viewPager: ViewPager2 = binding.viewPager
        viewPager.adapter = sectionsPagerAdapter

        // To disable swiping
        viewPager.isUserInputEnabled = false

        val tabs: TabLayout = binding.tabs

        // TabLayout requires a TabLayoutMediator instance to integrate with ViewPager2
        // The TabLayoutMediator object also handles the task
        // of generating page titles for the TabLayout object.
        // Previously: SectionsPagerAdapter.getPageTitle()
        TabLayoutMediator(tabs, viewPager) { tab, position ->
            tab.text = this.resources.getString(TAB_TITLES[position])
        }.attach()

        val fab: FloatingActionButton = binding.fab

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
    }
}
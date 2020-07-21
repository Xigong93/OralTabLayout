package pokercc.android.oraltablayout

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import pokercc.android.oraltablayout.databinding.ActivityMainBinding
import pokercc.android.oraltablayout.databinding.OralFragmentBinding

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.viewPager.adapter = OralAdapter(supportFragmentManager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }
}

class OralFragment : Fragment() {
    companion object {
        private const val POSITION = "position"
        fun newInstance(position: Int) = OralFragment().apply {
            arguments = bundleOf(POSITION to position)
        }
    }

    private val position by lazy { arguments?.getInt(POSITION) ?: 0 }
    private lateinit var binding: OralFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = OralFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.textView.text = (position + 1).toString()
    }
}

private class OralAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int) = OralFragment.newInstance(position)

    override fun getCount(): Int = 2

}
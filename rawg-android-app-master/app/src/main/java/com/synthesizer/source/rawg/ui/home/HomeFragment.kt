package com.synthesizer.source.rawg.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.synthesizer.source.rawg.core.domain.model.SearchParams
import com.synthesizer.source.rawg.databinding.FragmentHomeBinding
import com.synthesizer.source.rawg.ui.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch


@AndroidEntryPoint
class HomeFragment : BaseFragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override val viewModel: HomeViewModel by viewModels()
    private val adapter = HomeGamesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        registerListeners()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            homeScreenGames.offscreenPageLimit = 3
            (homeScreenGames[0] as RecyclerView).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
            homeScreenGames.setPageTransformer(OffsetPageTransformer(24, 24))
            homeScreenGames.adapter = adapter
            viewLifecycleOwner.lifecycleScope.launchWhenCreated {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    launch {
                        viewModel.images.filterNotNull().collect {
                            adapter.submitList(it)
                        }
                    }
                    launch {
                        viewModel.searchParams.collect {
                            navigateToList(it)
                        }
                    }
                }
            }
        }
    }

    private fun registerListeners() = binding.apply {
        adapter.imageLoadedListener = {
            if (selectedGameImageView.drawable == null)
                drawSelectedImage(homeScreenGames.currentItem)
        }

        homeScreenGames.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                drawSelectedImage(position)
            }
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchQuery(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }

        })

        bestOfTheYearGamesButton.setOnClickListener {
            viewModel.searchBestGamesOfTheYear()
        }

        popularGamesButton.setOnClickListener {
            viewModel.searchPopularGamesIn2022()
        }

        seeMoreGamesButton.setOnClickListener {
            viewModel.seeMoreGames()
        }
    }

    private fun navigateToList(searchParams: SearchParams) {
        val action = HomeFragmentDirections.showGames(searchParams)
        findNavController().navigate(action)
    }

    private fun drawSelectedImage(position: Int) {
        binding.apply {
            val viewHolder =
                (homeScreenGames[0] as RecyclerView).findViewHolderForAdapterPosition(
                    position
                ) as HomeGamesAdapter.ViewHolder
            val drawable = viewHolder.binding.homeScreenGameImage.drawable
            selectedGameImageView.setImageDrawable(drawable)
        }
    }
}
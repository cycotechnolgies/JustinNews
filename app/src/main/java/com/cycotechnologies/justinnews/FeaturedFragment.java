package com.cycotechnologies.justinnews; // Ensure this matches your package name

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button; // Import Button
import androidx.cardview.widget.CardView; // Import CardView if cardViewLiked is a CardView

/**
 * A simple {@link Fragment} subclass for displaying featured news categories.
 * This fragment handles button clicks for various news categories and navigates
 * to the CategoryNewsFragment by utilizing the hosting Activity's fragment replacement method.
 */
public class FeaturedFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FeaturedFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FeaturedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FeaturedFragment newInstance(String param1, String param2) {
        FeaturedFragment fragment = new FeaturedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_featured, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize buttons using the inflated 'view'
        Button btnTrending = view.findViewById(R.id.btnTrending);
        Button btnLocal = view.findViewById(R.id.btnLocal);
        Button btnWeather = view.findViewById(R.id.btnWeather);
        Button btnBusiness = view.findViewById(R.id.btnBusiness);
        Button btnTechnology = view.findViewById(R.id.btnTechnology);
        Button btnSocial = view.findViewById(R.id.btnSocial);
        Button btnSports = view.findViewById(R.id.btnSports);
        Button btnHealth = view.findViewById(R.id.btnHealth);
        CardView cardViewLiked = view.findViewById(R.id.cardViewLiked); // Assuming it's a CardView

        // Set OnClickListener for each category button
        btnTrending.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Trending")));
        btnLocal.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Local")));
        btnWeather.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Weather")));
        btnBusiness.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Business")));
        btnTechnology.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Technology")));
        btnSocial.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Social")));
        btnSports.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Sports")));
        btnHealth.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Health")));

        // Set OnClickListener for the "Liked" CardView
        cardViewLiked.setOnClickListener(v -> ((MainActivity) requireActivity()).replaceFragment(CategoryNewsFragment.newInstance("Liked")));
    }
}

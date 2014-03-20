package com.mdw.moneyconfig.proportion;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mdw.moneyconfig.R;

public class ProportionFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View proportionLayout = inflater.inflate(R.layout.proportion_layout, container,
				false);
		return proportionLayout;
	}

}

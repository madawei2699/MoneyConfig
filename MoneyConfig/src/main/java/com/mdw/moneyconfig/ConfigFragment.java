package com.mdw.moneyconfig;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.mdw.moneyconfig.DataService;

public class ConfigFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View configLayout = inflater.inflate(R.layout.config_layout,
				container, false);
		return configLayout;
	}

}

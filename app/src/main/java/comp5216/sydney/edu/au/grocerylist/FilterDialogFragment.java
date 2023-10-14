package comp5216.sydney.edu.au.grocerylist;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import comp5216.sydney.edu.au.grocerylist.data.dao.FoodDao;
import comp5216.sydney.edu.au.grocerylist.data.database.FreshPalDB;

public class FilterDialogFragment extends DialogFragment implements View.OnClickListener {
    public static final String TAG = "FilterDialog";
    interface FilterListener {

        void onFilter(Filters filters);

    }

    private View mRootView;

    private Spinner mCategorySpinner;
    private Spinner mStorageSpinner;
    private EditText mFromSpinner;
    private EditText mToSpinner;
    private Spinner mProductNameSpinner;

    private FilterListener mFilterListener;
    private ArrayList<String> categorySpinner = new ArrayList<>();
    private ArrayList<String> StorageSpinner = new ArrayList<>();
    private ArrayList<String> foodNameSpinner = new ArrayList<>();
    private ArrayAdapter<String> categoryAdapter;
    private ArrayAdapter<String> foodNameAdapter;
    private ArrayAdapter<String> conditionAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);


        //activity传给spinner的值
        Bundle bundle = getArguments();
        if (bundle != null) {
            categorySpinner.add("ALL");
            categorySpinner.addAll(bundle.getStringArrayList("category"));
            StorageSpinner.add("ALL");
            StorageSpinner.addAll(bundle.getStringArrayList("condition"));
            foodNameSpinner.add("ALL");
            foodNameSpinner.addAll(bundle.getStringArrayList("foodName"));
        }

        mCategorySpinner = mRootView.findViewById(R.id.spinner_category);
        mStorageSpinner = mRootView.findViewById(R.id.spinner_storage);
        mProductNameSpinner = mRootView.findViewById(R.id.spinner_name);
        mFromSpinner = mRootView.findViewById(R.id.input_from);
        mToSpinner = mRootView.findViewById(R.id.input_to);

// 创建ArrayAdapter
        categoryAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, categorySpinner.stream()
                .filter(Objects::nonNull).collect(Collectors.toList()));
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        foodNameAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, foodNameSpinner.stream()
                .filter(Objects::nonNull).collect(Collectors.toList()));
        foodNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conditionAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, StorageSpinner.stream()
                .filter(Objects::nonNull).collect(Collectors.toList()));
        conditionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
// 为Spinner设置适配器
        mCategorySpinner.setAdapter(categoryAdapter);
        mProductNameSpinner.setAdapter(foodNameAdapter);
        mStorageSpinner.setAdapter(conditionAdapter);

        mRootView.findViewById(R.id.button_search).setOnClickListener(this);
        mRootView.findViewById(R.id.button_cancel).setOnClickListener(this);

        return mRootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof FilterListener) {
            mFilterListener = (FilterListener) context;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getDialog().getWindow().setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.button_search)
            onSearchClicked();
        else if (v.getId() == R.id.button_cancel){
            onCancelClicked();
        }
    }

    public void onSearchClicked() {
        if (mFilterListener != null) {
            mFilterListener.onFilter(getFilters());
        }
        dismiss();
    }

    public void onCancelClicked() {
        dismiss();
    }

    @Nullable
    private String getSelectedCategory() {
        String selected = (String) mCategorySpinner.getSelectedItem();
        if (getString(R.string.value_any_category).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedStorage() {
        String selected = (String) mStorageSpinner.getSelectedItem();
        if (getString(R.string.value_any_storage).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private String getSelectedName() {
        String selected = (String) mProductNameSpinner.getSelectedItem();
        if (getString(R.string.value_any_product).equals(selected)) {
            return null;
        } else {
            return selected;
        }
    }

    @Nullable
    private long getSelectedFrom() {
        if(mFromSpinner.getText().toString().isEmpty()){
            return -30000;
        } else {
            long selected = Long.parseLong(mFromSpinner.getText().toString());
            return selected;
        }
    }

    @Nullable
    private long getSelectedTo() {
        if (mToSpinner.getText().toString().isEmpty()) {
            return 30000;
        } else {
            long selected = Long.parseLong(mToSpinner.getText().toString());
            return selected;
        }
    }

    public void resetFilters() {
        if (mRootView != null) {
            mCategorySpinner.setSelection(0);
            mStorageSpinner.setSelection(0);
            mFromSpinner.setSelection(0);
            mToSpinner.setSelection(0);
            mProductNameSpinner.setSelection(0);
        }
    }

    public Filters getFilters() {
        Filters filters = new Filters();

        if (mRootView != null) {
            filters.setCategory(getSelectedCategory());
            filters.setStorage(getSelectedStorage());
            filters.setFrom(getSelectedFrom());
            filters.setTo(getSelectedTo());
            filters.setProductName(getSelectedName());
        }

        return filters;
    }
}

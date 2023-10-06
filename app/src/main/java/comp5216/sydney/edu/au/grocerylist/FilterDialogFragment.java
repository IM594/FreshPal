package comp5216.sydney.edu.au.grocerylist;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.dialog_filters, container, false);

        mCategorySpinner = mRootView.findViewById(R.id.spinner_category);
        mStorageSpinner = mRootView.findViewById(R.id.spinner_storage);
        mProductNameSpinner = mRootView.findViewById(R.id.spinner_name);
        mFromSpinner = mRootView.findViewById(R.id.input_from);
        mToSpinner = mRootView.findViewById(R.id.input_to);

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
    private String getSelectedFrom() {
        String selected = (String) mFromSpinner.getText().toString();
        return selected;
    }

    @Nullable
    private String getSelectedTo() {
        String selected = (String) mToSpinner.getText().toString();
        return selected;
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

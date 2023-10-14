package comp5216.sydney.edu.au.grocerylist;

import androidx.lifecycle.ViewModel;

public class MainPageViewModel extends ViewModel {

    private boolean mIsSigningIn;
    private Filters mFilters;

    public MainPageViewModel() {
        mIsSigningIn = false;
        mFilters = Filters.getDefault();
    }

    public boolean getIsSigningIn() {
        return mIsSigningIn;
    }

    public void setIsSigningIn(boolean mIsSigningIn) {
        this.mIsSigningIn = mIsSigningIn;
    }

    public Filters getFilters() {
        return mFilters;
    }

    public void setFilters(Filters mFilters) {
        this.mFilters = mFilters;
    }
}




package de.franziskaneum.vplan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import de.franziskaneum.FranzCallback;
import de.franziskaneum.Status;
import de.franziskaneum.R;
import de.franziskaneum.drawer.DrawerFragment;
import de.franziskaneum.settings.SettingsManager;
import de.franziskaneum.views.SwipeRefreshLayout;

/**
 * Created by Niko on 20.12.2015.
 */
public class VPlanFragment extends DrawerFragment implements
        android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    private VPlanManager vplanManager;
    private VPlan vplan;
    private VPlanNotification vplanNotification;

    private boolean shouldRefresh;
    private VPlanRecyclerAdapter recyclerAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton floatingActionButton;
    private RelativeLayout errorContainer;
    private ImageView errorImage;
    private TextView errorDescription;

    private FranzCallback vplanCallback = new FranzCallback() {
        @Override
        public void onCallback(int status, Object... objects) {
            if (errorContainer != null)
                errorContainer.setVisibility(View.GONE);

            if (status == Status.OK && objects.length > 1 && objects[1] != null) {
                VPlanManager.Mode mode = (VPlanManager.Mode) objects[0];
                VPlan vplan = (VPlan) objects[1];
                VPlan tmpVPlan = VPlanFragment.this.vplan;

                VPlanFragment.this.vplan = vplan;
                if (recyclerAdapter != null)
                    recyclerAdapter.setVPlanData(vplan);
                getChangesData();

                if (mode == VPlanManager.Mode.CACHE && tmpVPlan == null) {
                    setRefreshing(true);
                    vplanManager.getVPlanAsync(VPlanManager.Mode.IF_MODIFIED, vplanCallback);
                } else
                    setRefreshing(false);
            } else {
                switch (status) {
                    case Status.AUTHENTICATION_NEEDED:
                        authenticationNeeded(false);
                        break;
                    case Status.FILE_NOT_FOUND:
                        setRefreshing(true);
                        vplanManager.getVPlanAsync(VPlanManager.Mode.DOWNLOAD, vplanCallback);
                        break;
                    case Status.CONTENT_NOT_MODIFIED:
                        if (vplan == null)
                            vplanManager.getVPlanAsync(VPlanManager.Mode.DOWNLOAD, vplanCallback);
                        else
                            setRefreshing(false);
                        break;
                    case Status.NO_CONNECTION:
                        setRefreshing(false);
                        if (VPlanFragment.this.vplan == null) {
                            if (errorDescription != null)
                                errorDescription.setText(R.string.no_connection);
                            if (errorImage != null)
                                errorImage.setImageDrawable(ContextCompat.getDrawable(
                                        getContext(), R.drawable.ic_no_connection));
                            errorContainer.setVisibility(View.VISIBLE);
                        } else if (swipeRefreshLayout != null)
                            Snackbar.make(swipeRefreshLayout, R.string.no_connection,
                                    Snackbar.LENGTH_LONG).show();
                        break;
                    default:
                        setRefreshing(false);
                        if (swipeRefreshLayout != null) {
                            Snackbar.make(swipeRefreshLayout, R.string.unknown_error,
                                    Snackbar.LENGTH_LONG).show();
                        }
                        break;
                }
            }
        }
    };

    private BroadcastReceiver newVPlanAvailableBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(VPlanNotificationManager.ACTION_NEW_VPLAN_AVAILABLE)) {
                VPlan vplan = intent.getParcelableExtra(VPlan.EXTRA_VPLAN);
                VPlanFragment.this.vplan = vplan;
                if (recyclerAdapter != null)
                    recyclerAdapter.setVPlanData(vplan);
                getChangesData();
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vplanManager = VPlanManager.getInstance();

        if (vplan == null) {
            vplanManager.getVPlanAsync(VPlanManager.Mode.CACHE, vplanCallback);
        }

        IntentFilter newVPlanAvailableIntentFilter =
                new IntentFilter(VPlanNotificationManager.ACTION_NEW_VPLAN_AVAILABLE);
        getActivity().registerReceiver(newVPlanAvailableBroadcastReceiver,
                newVPlanAvailableIntentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(newVPlanAvailableBroadcastReceiver);
    }

    private void authenticationNeeded(boolean didEnterWrongPassword) {
        if (errorContainer != null)
            errorContainer.setVisibility(View.GONE);

        if (isAdded()) {
            AlertDialog.Builder builder =
                    new AlertDialog.Builder(getActivity(), R.style.AlertDialogTheme);
            builder.setTitle(R.string.password);
            builder.setCancelable(false);

            if (didEnterWrongPassword)
                builder.setMessage(R.string.wrong_password);
            else
                builder.setMessage(R.string.message_enter_password);

            // inflate the dialog main view
            View dialogView =
                    LayoutInflater.from(getActivity()).inflate(R.layout.dialog_vplan_password, null);
            final EditText passwordEditText =
                    (EditText) dialogView.findViewById(R.id.dialog_vplan_password);

            // set the dialog main view
            builder.setView(dialogView);

            // add buttons to the dialog
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    setRefreshing(true);
                    String password = passwordEditText.getText().toString();

                    vplanManager.authenticateAsync(password, new FranzCallback() {
                        @Override
                        public void onCallback(int status, @Nullable Object... objects) {
                            if (Status.OK == status)
                                vplanManager.getVPlanAsync(VPlanManager.Mode.IF_MODIFIED, vplanCallback);
                            else
                                authenticationNeeded(true);
                        }
                    });
                }
            });

            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    if (vplan == null) {
                        setRefreshing(false);
                        if (errorDescription != null)
                            errorDescription.setText(R.string.unknown_error);
                        if (errorImage != null)
                            errorImage.setImageDrawable(null);

                        errorContainer.setVisibility(View.VISIBLE);
                    }
                }
            });

            // show the dialog
            builder.show();
        }
    }

    private void getChangesData() {
        if (vplan != null) {
            VPlanNotificationManager.getInstance().getNotificationFromVPlanAsync(vplan, new FranzCallback() {
                @Override
                public void onCallback(int status, Object... objects) {
                    if (Status.OK == status && objects.length > 0 && objects[0] != null) {
                        VPlanFragment.this.vplanNotification = (VPlanNotification) objects[0];
                        if (recyclerAdapter != null)
                            recyclerAdapter.setNotificationData(vplanNotification);
                    }
                }
            });
        }
    }

    private void setRefreshing(boolean refreshing) {
        shouldRefresh = refreshing;

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.setRefreshing(refreshing);

        if (floatingActionButton != null)
            if (refreshing) {
                floatingActionButton.show(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onShown(FloatingActionButton fab) {
                        super.onShown(fab);
                        fab.setVisibility(View.VISIBLE);
                    }
                });
            } else {
                floatingActionButton.hide(new FloatingActionButton.OnVisibilityChangedListener() {
                    @Override
                    public void onHidden(FloatingActionButton fab) {
                        super.onHidden(fab);
                        fab.setVisibility(View.INVISIBLE);
                    }
                });
            }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_vplan, container, false);

        Toolbar toolbar = (Toolbar) root.findViewById(R.id.toolbar);
        setToolbar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) root.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.ColorAccent);
        swipeRefreshLayout.setOnRefreshListener(this);

        floatingActionButton = (FloatingActionButton) root.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(this);

        setRefreshing(shouldRefresh);

        RecyclerView recyclerView = (RecyclerView) root.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (recyclerAdapter == null)
            recyclerAdapter = new VPlanRecyclerAdapter(this);

        if (vplan != null && !recyclerAdapter.hasData())
            recyclerAdapter.setVPlanData(vplan);

        recyclerAdapter.setNotificationData(vplanNotification);

        recyclerView.setAdapter(recyclerAdapter);

        errorContainer = (RelativeLayout) root
                .findViewById(R.id.error_container);
        errorImage = (ImageView) errorContainer.findViewById(R.id.error_image);
        errorDescription = (TextView) errorContainer
                .findViewById(R.id.error_description);

        errorContainer.setOnClickListener(this);

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        getChangesData();
    }

    @Override
    public void onRefresh() {
        vplanManager.getVPlanAsync(VPlanManager.Mode.IF_MODIFIED, vplanCallback);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab:
                setRefreshing(false);
                if (vplan == null) {
                    if (errorDescription != null)
                        errorDescription.setText(R.string.no_connection);
                    if (errorImage != null)
                        errorImage.setImageDrawable(ContextCompat.getDrawable(
                                getContext(), R.drawable.ic_no_connection));

                    if (errorContainer != null)
                        errorContainer.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.error_container:
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.setRefreshing(true);
                if (errorContainer != null)
                    errorContainer.setVisibility(View.GONE);
                onRefresh();
                break;
            default:
                Object itemPositionTag = view.getTag(R.string.recycler_item_position);

                if (itemPositionTag != null) {
                    int itemPosition = (int) itemPositionTag;
                    VPlan.VPlanDayData vplanDay = vplan.get(itemPosition);

                    Intent vplanDayIntent = new Intent(getActivity(), VPlanDayActivity.class);
                    vplanDayIntent.putExtra(VPlanDayActivity.EXTRA_VPLAN_DAY_DATA, vplanDay);
                    startActivity(vplanDayIntent);
                }
                break;
        }
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.vplan);
    }
}

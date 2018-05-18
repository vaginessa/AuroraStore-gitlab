package com.dragons.aurora.view;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dragons.aurora.InstallerFactory;
import com.dragons.aurora.R;
import com.dragons.aurora.Util;
import com.dragons.aurora.activities.AuroraActivity;
import com.dragons.aurora.activities.DetailsActivity;
import com.dragons.aurora.activities.ManualDownloadActivity;
import com.dragons.aurora.downloader.DownloadProgressBarUpdaterList;
import com.dragons.aurora.downloader.DownloadState;
import com.dragons.aurora.fragment.UpdatableAppsFragment;
import com.dragons.aurora.fragment.details.ButtonDownload;
import com.dragons.aurora.fragment.details.ButtonUninstall;
import com.dragons.aurora.notification.CancelDownloadService;
import com.dragons.aurora.task.playstore.PurchaseTask;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.dragons.aurora.Util.isAlreadyQueued;

public class UpdatableAppBadge extends AppBadge {


    @BindView(R.id.single_cancel)
    public Button cancel;
    @BindView(R.id.single_install)
    public Button install;
    @BindView(R.id.single_update)
    public Button update;
    @BindView(R.id.progress_bar_list)
    public ProgressBar progressBar;
    @BindView(R.id.progress_txt_list)
    public TextView progressCents;
    public boolean isDownloading = false;

    @BindView(R.id.single_uninstall)
    Button uninstall;
    @BindView(R.id.single_manual)
    Button manual;
    @BindView(R.id.viewChanges)
    ImageView viewChanges;
    @BindView(R.id.changes_upper)
    TextView changes;
    @BindView(R.id.list_container)
    LinearLayout listContainer;
    @BindView(R.id.changes_container)
    LinearLayout changesContainer;
    @BindView(R.id.progress_container)
    LinearLayout progressContainer;
    @BindView(R.id.single_buttons)
    LinearLayout singleButtons;

    @Override
    public void draw() {
        ButterKnife.bind(this, view);
        super.context = view.getContext();
        line2.clear();
        line3.clear();
        if (!TextUtils.isEmpty(app.getUpdated())) {
            line2.add(Formatter.formatShortFileSize(context, app.getSize()));
            line3.add(context.getString(R.string.list_line_2_updatable, app.getUpdated()));
        }
        if (app.isSystem()) {
            line3.add(context.getString(R.string.list_app_system));
        }
        drawIcon((ImageView) view.findViewById(R.id.icon));
        drawMore();
        super.draw();
    }

    @Override
    protected void drawIcon(ImageView icon) {
        super.drawIcon(icon);
        Bitmap bitmap = getBitmapFromDrawable(icon.getDrawable());
        getPalette(bitmap);
    }

    private void drawMore() {
        viewChanges.setOnClickListener(v -> {
            if (changesContainer.getVisibility() == View.GONE) {
                drawChanges();
                if (isAlreadyQueued(app)) {
                    new DownloadProgressBarUpdaterList(context, this)
                            .execute(PurchaseTask.UPDATE_INTERVAL);
                    hide(update);
                    show(cancel);
                }
            } else removeChanges();
        });
    }

    public void drawButtons() {
        init();

        listContainer.setOnClickListener(click ->
                context.startActivity(DetailsActivity.getDetailsIntent(context, app.getPackageName())));

        manual.setOnClickListener(click -> {
            ManualDownloadActivity.app = app;
            context.startActivity(new Intent(context, ManualDownloadActivity.class));
        });

        uninstall.setOnClickListener(click -> {
            new ButtonUninstall((AuroraActivity) context, app).uninstall();
            UpdatableAppsFragment.recheck = true;
        });

        cancel.setOnClickListener(click -> {
            context.startService(
                    new Intent(context.getApplicationContext(), CancelDownloadService.class)
                            .putExtra(CancelDownloadService.PACKAGE_NAME, app.getPackageName()));
            hide(cancel);
            show(update);
            isDownloading = false;
        });

        update.setOnClickListener(click -> {
            UpdatableAppBadge updatableAppBadge = this;
            isDownloading = true;
            new ButtonDownload((AuroraActivity) context, app).checkAndDownload();
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    DownloadState state = DownloadState.get(app.getPackageName());
                    if (state != null && !state.isEverythingSuccessful()) {
                        new DownloadProgressBarUpdaterList(context, updatableAppBadge).execute(PurchaseTask.UPDATE_INTERVAL);
                        this.cancel();
                    }
                }
            }, 0, 1000);
            hide(update);
            show(cancel);
        });

        install.setOnClickListener(v -> {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                    .cancel(app.getDisplayName().hashCode());
            InstallerFactory.get(context).verifyAndInstall(app);
            UpdatableAppsFragment.recheck = true;
        });
    }

    private void init() {
        if (Util.shouldDownload(context, app)) {
            show(update);
            hide(cancel);
        }

        if (Util.isAlreadyDownloaded(context, app)) {
            show(install);
            hide(cancel);
            hide(update);
        }
    }

    private void drawChanges() {
        viewChanges.setImageResource(R.drawable.ic_expand_less_24);

        if (app.getChanges().isEmpty())
            changes.setText(R.string.details_changelog_empty);
        else
            changes.setText(Html.fromHtml(app.getChanges()).toString());

        show(changesContainer);
        show(singleButtons);
        show(progressContainer);
    }

    private void removeChanges() {
        hide(changesContainer);
        hide(singleButtons);
        hide(progressContainer);
        viewChanges.setImageResource(R.drawable.ic_expand_more_24);
    }

    private void getPalette(Bitmap bitmap) {
        Palette.from(bitmap)
                .generate(palette -> {
                    paintTextView(palette.getDarkVibrantColor(Color.DKGRAY), changes);
                    paintButton(palette.getDarkVibrantColor(Color.DKGRAY), update);
                    paintButton(palette.getDarkVibrantColor(Color.DKGRAY), install);
                    paintLayout(palette.getDarkVibrantColor(Color.DKGRAY), R.id.view_background);
                    paintLLayout(palette.getDarkVibrantColor(Color.DKGRAY), R.id.changes_container);
                });
    }

}

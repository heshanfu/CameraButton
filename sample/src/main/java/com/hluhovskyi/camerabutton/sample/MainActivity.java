/*
 * Copyright (C) 2017 Artem Hluhovskyi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hluhovskyi.camerabutton.sample;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hluhovskyi.camerabutton.CameraButton;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends BaseActivity {

    private static final long ANIMATION_TRANSLATION_DURATION = 200L;

    static final List<String> CAMERA_MODE_NAMES = Collections.unmodifiableList(Arrays.asList(
            "Normal",
            "Boomerang",
            "Superzoom",
            "Rewind",
            "Hands-free",
            "Normal",
            "Boomerang",
            "Superzoom",
            "Rewind",
            "Hands-free"
    ));

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getCameraButton().setIcons(new Bitmap[]{
                BitmapHelper.getBitmap(this, R.drawable.ic_brightness_1_red_28dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_flash_on_red_36dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_sync_red_36dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_brightness_1_red_28dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_flash_on_red_36dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_sync_red_36dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_brightness_1_red_28dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_flash_on_red_36dp),
                BitmapHelper.getBitmap(this, R.drawable.ic_sync_red_36dp),
        });

        getCameraButton().setOnPhotoEventListener(() -> {
            getCameraButton().scrollIconsToPosition(15.5f);
        });

        getCameraButton().postDelayed(() -> {
            getCameraButton().cancel(false);
        }, 3000);

        getCameraButton().setOnVideoEventListener(new CameraButton.OnVideoEventListener() {
            @Override
            public void onStart() {
                startRecordVideo();
            }

            @Override
            public void onFinish() {
                finishRecordVideo();
            }

            @Override
            public void onCancel() {
            }
        });

        getCameraButton().setOnStateChangeListener(this::onStateChanged);

        new LinearSnapHelper().attachToRecyclerView(getModesRecycler());

        getModesRecycler().setAdapter(new CameraModeAdapter());
        getModesRecycler().setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));

        /*getModesRecycler().addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
                    throw new IllegalStateException("Only LinearLayoutManager is supported");
                }

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int range = recyclerView.computeHorizontalScrollRange();
                int extent = recyclerView.computeHorizontalScrollExtent();
                int offset = recyclerView.computeHorizontalScrollOffset();
                int first = manager.findFirstVisibleItemPosition();
                int last = manager.findLastVisibleItemPosition();

                View firstView = manager.findViewByPosition(first);


                Log.v("Recycler", "range = " + range +
                        ", extent = " + extent +
                        ", offset = " + offset +
                        ", first = " + first +
                        ", last = " + last +
                        ", viewLeft = " + firstView.getLeft() +
                        ", width = " + firstView.getWidth());


                float position = first + (float) Math.abs(Math.abs(firstView.getLeft()) - recyclerView.getPaddingLeft()) / firstView.getWidth();

                Log.v("Recycler", "position = " + position);

                getCameraButton().setIconsPosition(position);
            }
        });*/
        getModesRecycler().post(() -> {
            View view = getModesRecycler().getLayoutManager().findViewByPosition(0);
            int padding = getModesRecycler().getWidth() / 2 - 1;
            getModesRecycler().setPadding(
                    padding, 0, padding, 0
            );
        });
        getModesRecycler().addOnScrollListener(new RecyclerView.OnScrollListener() {

            int maxCenter = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!(recyclerView.getLayoutManager() instanceof LinearLayoutManager)) {
                    throw new IllegalStateException("Only LinearLayoutManager is supported");
                }

                LinearLayoutManager manager = (LinearLayoutManager) recyclerView.getLayoutManager();

                int offset = recyclerView.computeHorizontalScrollOffset();
                int first = manager.findFirstVisibleItemPosition();

                Log.v("Recycler", "BEGIN -------------->");

                View firstView = manager.findViewByPosition(first);
                if (firstView == null) {
                    Log.v("Recycler", "I DONT GET A FUCK WHAT IS GOING ON HERE, position = " + first);
                    return;
                }

                int firstViewCenter = firstView.getWidth() / 2;

                float diff = 0;
                if (recyclerView.getPaddingLeft() != 0) {
                    diff = Math.abs(recyclerView.getWidth() / 2f - recyclerView.getPaddingLeft() - firstViewCenter);
                } else {
                    diff = 0;
                }

                Log.v("Recycler", "first [left=" + firstView.getLeft()
                        + ",width=" + firstView.getWidth()
                        + ",paddingLeft=" + recyclerView.getPaddingLeft()
                        + "]");
                Log.v("Recycler", "END <--------------");

                float position = first - 0.5f + Math.abs((float) (firstView.getLeft() - recyclerView.getPaddingLeft() + diff - firstViewCenter) / firstView.getWidth());
                getCameraButton().setIconsPosition(position);

            }
        });

        getModesRecycler().getAdapter().registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            }
        });
    }

    private void onStateChanged(CameraButton.State state) {
        if (state == CameraButton.State.START_EXPANDING) {
            translateToRight(getFlashSwitch(), false);
            translateToLeft(getCameraSwitch(), false);
            translateToBottom(getModesRecycler(), false);
        } else if (state == CameraButton.State.START_COLLAPSING) {
            translateToRight(getFlashSwitch(), true);
            translateToLeft(getCameraSwitch(), true);
            translateToBottom(getModesRecycler(), true);
        }
    }

    void makePhoto() {
    }

    void startRecordVideo() {
    }

    void finishRecordVideo() {
    }

    private static void translateToRight(View view, boolean show) {
        float x = show ? 0f : view.getWidth();
        float alpha = show ? 1f : 0f;
        view.animate().translationX(-x)
                .alpha(alpha)
                .setDuration(ANIMATION_TRANSLATION_DURATION);
    }

    private static void translateToLeft(View view, boolean show) {
        float x = show ? 0f : view.getWidth();
        float alpha = show ? 1f : 0f;
        view.animate().translationX(x)
                .alpha(alpha)
                .setDuration(ANIMATION_TRANSLATION_DURATION);
    }

    private static void translateToBottom(View view, boolean show) {
        float y = show ? 0f : view.getHeight();
        float alpha = show ? 1f : 0f;
        view.animate().translationY(y)
                .alpha(alpha)
                .setDuration(ANIMATION_TRANSLATION_DURATION);
    }

    private static class CameraModeAdapter extends RecyclerView.Adapter<CameraModeAdapter.ViewHolder> {

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_camera_mode, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.mName.setText(CAMERA_MODE_NAMES.get(position));
        }

        @Override
        public int getItemCount() {
            return CAMERA_MODE_NAMES.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {

            final TextView mName;

            ViewHolder(View itemView) {
                super(itemView);
                mName = itemView.findViewById(R.id.name);
            }
        }
    }
}

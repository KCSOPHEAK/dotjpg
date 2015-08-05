/*
 * Copyright (c) 2015  Alashov Berkeli
 * It is licensed under GNU GPL v. 2 or later. For full terms see the file LICENSE.
 */

package tm.alashow.dotjpg.ui.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import butterknife.Bind;
import tm.alashow.dotjpg.Config;
import tm.alashow.dotjpg.R;
import tm.alashow.dotjpg.model.ListItem;
import tm.alashow.dotjpg.model.NewImage;
import tm.alashow.dotjpg.ui.adapter.NewImagesAdapter;
import tm.alashow.dotjpg.util.U;

/**
 * Created by alashov on 04/08/15.
 */
public class NewImageActivity extends BaseActivity {

    private ArrayList<NewImage> images = new ArrayList<>();
    private NewImagesAdapter newImagesAdapter;

    private File NEW_CAPTURED_IMAGE;

    @Bind(R.id.upload) View uploadView;
    @Bind(R.id.listView) ListView mListView;
    @Bind(R.id.empty) View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        newImagesAdapter = new NewImagesAdapter(this, images);
        mListView.setAdapter(newImagesAdapter);

        mListView.setEmptyView(emptyView);
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                alertDialogBuilder.setTitle(R.string.image_new_remove);
                alertDialogBuilder.setMessage(R.string.image_new_remove_description);
                alertDialogBuilder.setNegativeButton(R.string.no, null);
                alertDialogBuilder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (images.get(position).isCompressed()) {
                            images.get(position).getCompressedFile().delete();
                        }

                        images.remove(position);
                        newImagesAdapter.notifyDataSetChanged();
                    }
                });
                alertDialogBuilder.show();
                return true;
            }
        });

        emptyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddImageDialog();
            }
        });

        uploadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (images.isEmpty()) {
                    shakeIt(emptyView);
                } else { //upload 'em

                }
            }
        });

        //Receiving data
        Intent intent = getIntent();
        String intentAction = intent.getAction();
        String type = intent.getType();

        if (Intent.ACTION_SEND.equals(intentAction) || Intent.ACTION_SEND_MULTIPLE.equals(intentAction) && type != null) {
            if (type.startsWith("image/")) {
                if (Intent.ACTION_SEND.equals(intentAction)) {
                    Uri imageUri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        addImage(imageUri);
                    }
                } else if (Intent.ACTION_SEND_MULTIPLE.equals(intentAction)) {
                    ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    for(int i = 0; i < imageUris.size(); i++) {
                        if (isReachedMaxCount()) {
                            U.showError(emptyView, R.string.image_new_error_max_count);
                            break;
                        }
                        addImage(imageUris.get(i));
                    }
                }
            }
        }

        //if list is empty, show chooser
        if (images.size() == 0) {
            showAddImageDialog();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_image, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                showAddImageDialog();
                return true;
        }
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == U.RESULT_GALLERY) {
                addImage(data.getData());
            } else if (requestCode == U.RESULT_CAMERA) {
                if (NEW_CAPTURED_IMAGE.exists()) {
                    U.addMediaToGallery(NEW_CAPTURED_IMAGE.getAbsolutePath());
                    addImage(Uri.fromFile(NEW_CAPTURED_IMAGE));
                }
            }
        }
    }

    private void showAddImageDialog() {
        if (isReachedMaxCount()) {
            U.showError(emptyView, R.string.image_new_error_max_count);
            return;
        }

        final ListItem[] listItems = {
            new ListItem(getString(R.string.image_new_add_camera), R.drawable.ic_camera_grey),
            new ListItem(getString(R.string.image_new_add_gallery), R.drawable.ic_images_grey)
        };

        ListAdapter adapter = new ArrayAdapter<ListItem>(this, android.R.layout.select_dialog_item, android.R.id.text1, listItems) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = (TextView) view.findViewById(android.R.id.text1);

                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimensionPixelSize(R.dimen.text_large));
                textView.setCompoundDrawablesWithIntrinsicBounds(listItems[position].icon, 0, 0, 0);
                textView.setCompoundDrawablePadding((int) (5 * getResources().getDisplayMetrics().density + 0.5f));
                return view;
            }
        };

        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.image_new_add))
            .setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    switch (item) {
                        case 0:
                            NEW_CAPTURED_IMAGE = U.generatePicturePath();
                            U.imageCapture(getActivity(), NEW_CAPTURED_IMAGE);
                            break;
                        case 1:
                            U.imageChoose(getActivity());
                            break;
                    }
                }
            }).show();
    }

    private void addImage(Uri uriImage) {
        if (isReachedMaxCount()) {
            U.showError(emptyView, R.string.image_new_error_max_count);
            return;
        }

        File image = new File(U.getRealPathFromURI(this, uriImage));

        if (image.exists()) {
            if (image.length() < Config.API_IMAGE_MAX_FILE_SIZE) {
                images.add(new NewImage().setOriginFile(image));
                newImagesAdapter.notifyDataSetChanged();
            } else {
                U.showError(emptyView, R.string.image_new_error_max_size);
            }
        } else {
            U.showError(emptyView, R.string.image_new_error_not_found);
        }
    }

    private boolean isReachedMaxCount() {
        return images.size() >= Config.API_IMAGE_MAX_FILE_COUNT;
    }

    private void shakeIt(View target) {
        if (target == null) {
            return;
        }
        AnimatorSet mAnimatorSet = new AnimatorSet();
        mAnimatorSet.playTogether(ObjectAnimator.ofFloat(target, "translationX", 0, 25, - 25, 25, - 25, 15, - 15, 6, - 6, 0));
        mAnimatorSet.start();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_new_image;
    }

    @Override
    protected Boolean isChildActivity() {
        return true;
    }

    @Override
    protected String getActivityTag() {
        return Config.ACTIVITY_TAG_IMAGE_NEW;
    }
}
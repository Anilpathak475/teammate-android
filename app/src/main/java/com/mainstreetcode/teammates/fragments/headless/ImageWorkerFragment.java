package com.mainstreetcode.teammates.fragments.headless;

import android.app.Activity;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.mainstreetcode.teammates.R;
import com.mainstreetcode.teammates.baseclasses.TeammatesBaseFragment;
import com.mainstreetcode.teammates.util.ErrorHandler;
import com.theartofdev.edmodo.cropper.CropImage;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseFragment;
import com.tunjid.androidbootstrap.core.abstractclasses.BaseRecyclerViewAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Maybe;
import io.reactivex.MaybeEmitter;
import io.reactivex.MaybeOnSubscribe;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.M;
import static io.reactivex.android.schedulers.AndroidSchedulers.mainThread;
import static io.reactivex.schedulers.Schedulers.io;

/**
 * Inner fragment hosting code for interacting with image and cropping APIs
 */

public class ImageWorkerFragment extends TeammatesBaseFragment {

    public static final int CROP_CHOOSER = 1;
    public static final int MULTIPLE_MEDIA_CHOOSER = 2;

    public static final String TAG = "ImageWorkerFragment";
    public static final String IMAGE_SELECTION = "image/*";
    public static final String IMAGE_VIDEO_SELECTION = "image/* video/*";

    public static ImageWorkerFragment newInstance() {
        return new ImageWorkerFragment();
    }

    public static void attach(BaseFragment host) {
        if (getInstance(host) != null) return;

        ImageWorkerFragment instance = ImageWorkerFragment.newInstance();

        host.getChildFragmentManager().beginTransaction()
                .add(instance, makeTag(host))
                .commit();
    }

    public static void requestCrop(BaseFragment host) {
        ImageWorkerFragment instance = getInstance(host);

        if (instance == null) return;

        boolean noPermit = SDK_INT >= M && ContextCompat.checkSelfPermission(host.getActivity(),
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        if (noPermit)
            instance.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, CROP_CHOOSER);
        else instance.startImagePicker();
    }


    public static void requestMultipleMedia(BaseFragment host) {
        ImageWorkerFragment instance = getInstance(host);

        if (instance == null) return;

        boolean noPermit = SDK_INT >= M && ContextCompat.checkSelfPermission(host.getActivity(),
                WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;

        if (noPermit)
            instance.requestPermissions(new String[]{WRITE_EXTERNAL_STORAGE}, CROP_CHOOSER);
        else instance.startMultipleMediaPicker();
    }

    public static void detach(BaseFragment host) {
//        if (getInstance(host) != null) return;
//
//        ImageWorkerFragment instance = ImageWorkerFragment.newInstance();
//        instance.setTargetFragment(host, ImageWorkerFragment.GALLERY_CHOOSER);
//
//        host.getFragmentManager().beginTransaction()
//                .remove(instance)
//                .commit();
    }

    @Override
    public void onAttachFragment(Fragment childFragment) {
        super.onAttachFragment(childFragment);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CROP_CHOOSER:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startImagePicker();
                }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        boolean isCropListener = isCropListener();
        boolean isMediaListener = isMediaListener();

        Fragment target = getParentFragment();

        if (resultCode != Activity.RESULT_OK || (!isCropListener && !isMediaListener)) return;

        if (requestCode == CROP_CHOOSER && isCropListener) {
            CropImage.activity(data.getData())
                    .setFixAspectRatio(true)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(80, 80)
                    .setMaxCropResultSize(1000, 1000)
                    .setOutputCompressFormat(Bitmap.CompressFormat.PNG)
                    .start(getContext(), this);
        }
        else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && isCropListener) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            Uri resultUri = result.getUri();
            ((CropListener) target).onImageCropped(resultUri);
        }
        else if (requestCode == MULTIPLE_MEDIA_CHOOSER && isMediaListener) {
            MediaListener listener = (MediaListener) target;
            ContentResolver contentResolver = getContext().getContentResolver();

            Maybe<List<Uri>> filesMaybe = Maybe.create(new MediaQuery(data, contentResolver)).subscribeOn(io()).observeOn(mainThread());
            disposables.add(filesMaybe.subscribe(listener::onFilesSelected, ErrorHandler.EMPTY));
        }
    }

    boolean isCropListener() {
        Fragment target = getParentFragment();
        return target != null && target instanceof CropListener;
    }

    boolean isMediaListener() {
        Fragment target = getParentFragment();
        return target != null && target instanceof MediaListener;
    }

    private static String makeTag(BaseFragment host) {
        return TAG + "-" + host.getStableTag();
    }

    @Nullable
    private static ImageWorkerFragment getInstance(BaseFragment host) {
        return (ImageWorkerFragment) host.getChildFragmentManager().findFragmentByTag(makeTag(host));
    }

    private void startImagePicker() {
        Intent intent = new Intent();
        intent.setType(IMAGE_SELECTION);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, getString(R.string.pick_picture)), CROP_CHOOSER);
    }

    private void startMultipleMediaPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType(IMAGE_VIDEO_SELECTION);
        if (SDK_INT >= JELLY_BEAN_MR2) intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);

        startActivityForResult(intent, MULTIPLE_MEDIA_CHOOSER);
    }

    public interface CropListener {
        void onImageCropped(Uri uri);
    }

    public interface MediaListener {
        void onFilesSelected(List<Uri> uris);
    }

    public interface ImagePickerListener extends BaseRecyclerViewAdapter.AdapterListener {
        void onImageClick();
    }

    static class MediaQuery implements MaybeOnSubscribe<List<Uri>> {

        static final String[] FILE_PATH_COLUMN = {MediaStore.Images.Media.DATA};

        private Intent data;
        private ContentResolver contentResolver;

        MediaQuery(Intent data, ContentResolver contentResolver) {
            this.data = data;
            this.contentResolver = contentResolver;
        }

        @Override
        public void subscribe(MaybeEmitter<List<Uri>> emitter) throws Exception {
            emitter.onSuccess(onData());
        }

        private List<Uri> onData() {
            List<Uri> files = new ArrayList<>();

            if (data.getClipData() != null) {
                ClipData clipData = data.getClipData();
                int count = clipData.getItemCount();

                for (int i = 0; i < count; i++) {
                    File file = fromUri(clipData.getItemAt(i).getUri());
                    if (file != null) files.add(Uri.fromFile(file));
                }
            }
            else if (data.getData() != null) {
                File file = fromUri(data.getData());
                if (file != null) files.add(Uri.fromFile(file));
            }
            return files;
        }

        @Nullable
        private File fromUri(Uri uri) {
            Cursor cursor = contentResolver.query(uri, FILE_PATH_COLUMN, null, null, null);

            if (cursor == null) return null;

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(FILE_PATH_COLUMN[0]);
            String filePath = cursor.getString(columnIndex);

            cursor.close();

            File file = new File(filePath);
            return file.exists() ? file : null;
        }
    }
}
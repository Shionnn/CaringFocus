package sg.edu.np.mad.voiceassist;

import android.content.Context;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.storage.StorageReference;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import java.io.InputStream;

@GlideModule
public class MyAppGlideModule extends AppGlideModule {
    //using glide to set images from firebase https://guides.codepath.com/android/Displaying-Images-with-the-Glide-Library
    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference.class, InputStream.class,
                new FirebaseImageLoader.Factory());
    }
}

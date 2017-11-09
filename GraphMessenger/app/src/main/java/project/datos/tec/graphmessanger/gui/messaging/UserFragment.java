package project.datos.tec.graphmessanger.gui.messaging;


import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.facebook.Profile;
import java.util.concurrent.ExecutionException;
import project.datos.tec.graphmessanger.R;
import project.datos.tec.graphmessanger.logic.communication.facebook.GetImg;
import project.datos.tec.graphmessanger.logic.datamanagement.SharedData;

import static android.app.DialogFragment.STYLE_NO_FRAME;


public class UserFragment extends Fragment {

    private View view;
    public UserFragment() {
    }



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_user, container, false);

        Profile profile = Profile.getCurrentProfile();

        //Agregar el nombre de Usuario
        String Name = profile.getFirstName();
        String Name2 = profile.getMiddleName();
        String Name3 = profile.getLastName();

        SharedData.instance().setUserName(Name+" "+Name2+" "+Name3);
        SharedData.instance().setUserID(profile.getId().toString());

        TextView textview = (TextView) view.findViewById(R.id.username);
        textview.setText(Name + " " + Name2 + " " + Name3);

        //Agregar la Imagen del Usuario
        GetImg getImg = new GetImg();
        Bitmap bitmap = null;
        try {
            bitmap = getImg.execute(profile.getId()).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        Drawable drawable = new BitmapDrawable(this.getResources(), bitmap);

        ImageView imagen = (ImageView) view.findViewById(R.id.circle_image);
        imagen.setImageDrawable(drawable);

        SharedData.instance().setUserImg(drawable);

        return view;
    }
}

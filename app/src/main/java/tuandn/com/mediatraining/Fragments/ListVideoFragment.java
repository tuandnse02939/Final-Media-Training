//package tuandn.com.mediatraining.Fragments;
//
//import android.os.Bundle;
//import android.support.v4.app.Fragment;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import tuandn.com.mediatraining.Model.YouTubeChannel;
//import tuandn.com.mediatraining.R;
//
//
///**
// * Created by Anh Trung on 7/8/2015.
// */
//public class ListVideoFragment extends Fragment {
//    public static final String ARG_PAGE = "ARG_PAGE";
//    public static final String API_KEY = "AIzaSyD0MwUad7hVnPWiuX5HiOWCEnf2VVGd8gY";
//
//    private ArrayList<YouTubeChannel> channels;
//
//    private int mPage;
//
//    public static ListVideoFragment newInstance(int page) {
//        Bundle args = new Bundle();
//        args.putInt(ARG_PAGE, page);
//        ListVideoFragment fragment = new ListVideoFragment();
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mPage = getArguments().getInt(ARG_PAGE);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_page, container, false);
//        return view;
//    }
//
//    @Override
//    public void onActivityCreated(Bundle savedInstanceState) {
//        channels = new ArrayList<YouTubeChannel>();
//        new getListChannels().excute();
//
//        super.onActivityCreated(savedInstanceState);
//    }
//
//    class getListChannels extends AsyncTask<Void, Void, ArrayList<Friend>> {
//
//        private Exception exception;
//
//        protected ArrayList<YouTubeChannel> doInBackground(Void... voids) {
//            //Get Friend List
//            try {
//
//            } catch (Exception e){
//                e.printStackTrace();
//            }
//            return friendList;
//        }
//
//        protected void onPostExecute(ArrayList<Friend> frList) {
//            if(frList.size() != 0) {
//                setListAdapter(new FriendListAdapter(DisplayFriendListActivity.this, frList));
//            }
//            else {
//                frList = handler.getFriends();
//                if(frList.size() != 0) {
//                    setListAdapter(new FriendListAdapter(DisplayFriendListActivity.this, frList));
//                }
//                else {
//                    Toast.makeText(getApplication(), "Can not get Friends", Toast.LENGTH_LONG).show();
//                }
//            }
//        }
//    }
//}

/**
 * 
 */
package com.tiago.bobby.listener;

import java.util.List;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.tiago.bobby.MainActivity;
import com.tiago.bobby.tasks.ListFriendsTask;
import com.tiago.bobby.types.ListFriendsArray;
import com.tiago.bobby.types.Person;

/**
 * @author tfr_souza
 *
 */
public class ButtonListFriendsClick implements OnClickListener {

	private EditText mPersonID;
	private ListView mListView;
	
	public ButtonListFriendsClick(EditText personID, ListView listView) {
		mPersonID = personID;
		mListView = listView;
	}
	
	@Override
	public void onClick(View v) {
		try {
			if (MainActivity.checkInternetConnection(v.getContext())) {
				String id = mPersonID.getText().toString();
				
				ListFriendsTask task = new ListFriendsTask();
				List<Person> friends = task.execute(id).get();
				
				if (friends.size() > 0) {
					if (friends.get(0).getId() != -1) {
						ListFriendsArray listFriends = new ListFriendsArray(v.getContext(), friends);
						mListView.setAdapter(listFriends);
					} else {
						Toast.makeText(v.getContext(), "erro no app", Toast.LENGTH_LONG).show();
					}
				} else {
					Toast.makeText(v.getContext(), "sem amigos", Toast.LENGTH_LONG).show();
				}
				
				mPersonID.setText(null);
			} else {
				MainActivity.noConnection(v.getContext());
			}
		} catch (Exception e) {
			Toast.makeText(v.getContext(), e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
		}
	}

}

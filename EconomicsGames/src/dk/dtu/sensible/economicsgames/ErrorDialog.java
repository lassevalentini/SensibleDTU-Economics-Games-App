package dk.dtu.sensible.economicsgames;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ErrorDialog extends DialogFragment {

	private String message = "Error";
	
	@Override
	public void setArguments(Bundle bundle) {
		super.setArguments(bundle);
		
		if (bundle.containsKey("message")) {
			this.message = bundle.getString("message");
		}
	}
	
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
               .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       getActivity().finish();
                   }
               });
//               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                   public void onClick(DialogInterface dialog, int id) {
//                       // User cancelled the dialog
//                   }
//               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}
package data;

public class NoteContract extends Contract {

	public NoteContract(){
		super();
	}
	
	@Override
	public String tableName() {return "notes";}

}

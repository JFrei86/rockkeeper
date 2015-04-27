// I want to get XXXXX points all time. I'm currently at XXXXXX points.
// I want to atttempt XX routes and one day. I want to complete at least XX of them.
// I want to climb a VX by XX/XX/XXXX .

// Alex start start XML - get us an idea of what we're looking it - visualize it
// Jesse think about how the skelleton framework (null values in the database, column with climb/attempt/get...)
// Dropdown to choose question format and interchangeable verbs
// column to indicate type (difficulty, points, completed). THe columsn will be null except for column indicated by thte type.

package transcend.rockeeper.data;

public class GoalContract extends Contract {

	public GoalContract(){
		super();
	}
	
	@Override
	public String tableName() { return "goals";}

}

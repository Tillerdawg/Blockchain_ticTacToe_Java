/**
 * @author jasontiller
 *
 */

package noobchain; // this file is part of the noobchain package

// import needed security module: PublicKey
import java.security.PublicKey;

// Class for creating TransactionOutput objects
public class TransactionOutput {
	// The ID of a given TransactionOutput object
	public String id;
	// Also known as the new owner of these coins
	public PublicKey recipient;
	// The amount of coins they own
	public float value;
	// The ID of the transaction in which this output was created
	public String parentTransactionId;

	// A method for constructing TransactionOutput objects
	public TransactionOutput(PublicKey recipient, float value, String parentTransactionId) {
		// Sets the recipient of the transaction
		this.recipient = recipient;
		// Sets the value of the transaction
		this.value = value;
		// Sets the parent transaction's ID
		this.parentTransactionId = parentTransactionId;
		// creates a unique id value for the transaction
		this.id = StringUtil
				.applySha256(StringUtil.getStringFromKey(recipient) + Float.toString(value) + parentTransactionId);
	}

	// a boolean method for checking to see a coin belongs to the recipient
	public boolean isMine(PublicKey publicKey) {
		// returns true or false
		return (publicKey == recipient);
	}

}

/**
 * @author jasontiller
 *
 */
package noobchain; // this file is part of the noobchain package

// Import needed security and utility modules: Security, ArrayList, and HashMap
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;

// A class to create NoobChain objects
public class NoobChain {

	// Intialize NoobChain fields
	// Create an ArrayList Object to hold blockchain information
	public static ArrayList<Block> blockchain = new ArrayList<Block>();
	// Create a Hashmap Object to hold UTXOs information
	public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();
	// Set the difficulty for mining blocks
	public static int difficulty = 3;
	// Set a minimum transaction value
	public static float minimumTransaction = 0.1f;
	// Create a space in computer memory for walletA information
	public static Wallet walletA;
	// Create a space in computer memory for walletB information
	public static Wallet walletB;
	// Create a space in computer memory for genesisTransaction information
	public static Transaction genesisTransaction;

	// The Main method, which is where the program begins
	public static void main(String[] args) {
		// Setup Bouncy castle as a Security Provider
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

		// Initialize walletA to empty
		walletA = new Wallet();
		// Initialize walletB to empty
		walletB = new Wallet();
		// Create a new Wallet object called 'coinbase' and initialize to empty
		Wallet coinbase = new Wallet();

		// create the genesis transaction, which sends 100 NoobCoin to walletA:
		genesisTransaction = new Transaction(coinbase.publicKey, walletA.publicKey, 100f, null);
		genesisTransaction.generateSignature(coinbase.privateKey); // manually sign the genesis transaction
		genesisTransaction.transactionId = "0"; // manually set the transaction id
		genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.recipient, genesisTransaction.value,
				genesisTransaction.transactionId)); // manually add the Transactions Output
		UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0)); // its important to store
																							// our first transaction in
																							// the UTXOs list.
		// Print genesis block mining information to the console
		System.out.println("Creating and Mining Genesis block... ");
		// Create the genesis block
		Block genesis = new Block("0");
		// Add genesis transation information to the genesis block
		genesis.addTransaction(genesisTransaction);
		// Add the genesis block to the blockchain
		addBlock(genesis);

		// Create block1 using the genesisBlock's hash
		Block block1 = new Block(genesis.hash);
		// Print WalletA information to the console
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		// Print transaction intent to the console
		System.out.println("\nWalletA is Attempting to send funds (40) to WalletB...");
		// Add a transaction between walletA to walletB to block1
		block1.addTransaction(walletA.sendFunds(walletB.publicKey, 40f));
		// Add block1 to the blockchain
		addBlock(block1);
		// Print WalletA balance information to console
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		// Print WalletB balance information to console
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		// Create block2 using block1's hash
		Block block2 = new Block(block1.hash);
		// Print transaction intent to the console
		System.out.println("\nWalletA Attempting to send more funds (1000) than it has...");
		// Add a transaction between walletA to walletB to block2
		block2.addTransaction(walletA.sendFunds(walletB.publicKey, 1000f));
		// Add block2 to the blockchain
		addBlock(block2);
		// Print WalletA balance information to console
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		// Print WalletB balance information to console
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		// Create block3 using block2's hash
		Block block3 = new Block(block2.hash);
		// Print transaction intent to the console
		System.out.println("\nWalletB is Attempting to send funds (20) to WalletA...");
		// Add a transaction between walletB and walletA to block3
		block3.addTransaction(walletB.sendFunds(walletA.publicKey, 20));
		// Print WalletA balance information to console
		System.out.println("\nWalletA's balance is: " + walletA.getBalance());
		// Print WalletB balance information to console
		System.out.println("WalletB's balance is: " + walletB.getBalance());

		// Check to see if the blockchain is valid
		isChainValid();

	}

	// A boolean method to check to see if the blockchain is valid
	public static Boolean isChainValid() {
		// Create a Block object to hold information about the current block
		Block currentBlock;
		// Create a Block object to hold information about the previous block
		Block previousBlock;
		// Create a hashTarget using the difficulty
		String hashTarget = new String(new char[difficulty]).replace('\0', '0');
		// Create a HashMap object to hold a temporary working list of unspent
		// transactions at a given block state, called 'tempUTXOs.'
		HashMap<String, TransactionOutput> tempUTXOs = new HashMap<String, TransactionOutput>();
		// put genesisTransaction outputs information into tempUTXOs
		tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));

		// loop through blockchain to check hashes
		// for all parts of the blockchain ...
		for (int i = 1; i < blockchain.size(); i++) {

			// Set currentBlock to the value of the current iterator (i) position
			currentBlock = blockchain.get(i);
			// Set previousBlock to the value of the position before the iterator's position
			previousBlock = blockchain.get(i - 1);
			// compare registered hash and calculated hash:
			if (!currentBlock.hash.equals(currentBlock.calculateHash())) {
				// If the registered hash and calculated hash are unequal, print error message
				// to console and return false
				System.out.println("#Current Hashes not equal");
				return false;
			}
			// compare previous hash and registered previous hash
			if (!previousBlock.hash.equals(currentBlock.previousHash)) {
				// If the previous block's hash does not equal the currentBlock's previousHash
				// value, print error message to console and return false
				System.out.println("#Previous Hashes not equal");
				return false;
			}
			// check if hash is solved
			if (!currentBlock.hash.substring(0, difficulty).equals(hashTarget)) {
				// If the substring of the current block's hash does not equal the hashTarget,
				// print error message to the console and return false
				System.out.println("#This block hasn't been mined");
				return false;
			}

			// Create a TransactionOutput to temporarily store transaction output
			// information
			TransactionOutput tempOutput;
			// For each position of the currentBlock's transactions ...
			for (int t = 0; t < currentBlock.transactions.size(); t++) {
				// Create a new transaction object and set it to the current block's
				// transactions at position 't'
				Transaction currentTransaction = currentBlock.transactions.get(t);

				// If the currentTransaction's signature is not valid
				if (!currentTransaction.verifySignature()) {
					// Print error message to console and return false
					System.out.println("#Signature on Transaction(" + t + ") is Invalid");
					return false;
				}
				// If the inputs value to not match the outputs value
				if (currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
					// Print error message to console and return false
					System.out.println("#Inputs are note equal to outputs on Transaction(" + t + ")");
					return false;
				}

				// For each input in the current transaction's inputs ...
				for (TransactionInput input : currentTransaction.inputs) {
					// Set tempOutput to value of the transactionOutputID from the tempUTXOs object
					tempOutput = tempUTXOs.get(input.transactionOutputId);

					// Check to see if tempOutput does not exist
					if (tempOutput == null) {
						// Print error message to console and return false
						System.out.println("#Referenced input on Transaction(" + t + ") is Missing");
						return false;
					}

					// Check to see if the referrenced input transaction value is invalid
					if (input.UTXO.value != tempOutput.value) {
						// Print error message to console and return false
						System.out.println("#Referenced input Transaction(" + t + ") value is Invalid");
						return false;
					}

					// Remove the transactionOutputID from the tempUTXOs object
					tempUTXOs.remove(input.transactionOutputId);
				}

				// For each TransactionOutput object in the set of outputs in the
				// currentTransaction...
				for (TransactionOutput output : currentTransaction.outputs) {
					// Put the output's id and value into tempUTXOs
					tempUTXOs.put(output.id, output);
				}

				// Check to see if the output recipient is who it should be
				if (currentTransaction.outputs.get(0).recipient != currentTransaction.recipient) {
					// Print error message to console and return false
					System.out.println("#Transaction(" + t + ") output recipient is not who it should be");
					return false;
				}
				// Check to see if the sender is the recipient of the change from the
				// transaction
				if (currentTransaction.outputs.get(1).recipient != currentTransaction.sender) {
					// Print error message to console and return false
					System.out.println("#Transaction(" + t + ") output 'change' is not sender.");
					return false;
				}

			}

		}
		// Print success message to console and return true
		System.out.println("Blockchain is valid");
		return true;
	}

	// A method to add a new block to the blockchain
	public static void addBlock(Block newBlock) {
		// Mine the new block with the correct difficulty
		newBlock.mineBlock(difficulty);
		// Add the new block to the blockchain
		blockchain.add(newBlock);
	}
}

import java.io.IOException;

public class MainHill {

	public static void main(String[] args) {

		String key = "qwertyfki";

		Encryption encrypt = new Encryption(key);
		try {
			encrypt.getResult("OpenText.txt", "CryptoText.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

		Decryption decrypt = new Decryption(key);
		try {
			decrypt.getResult("CryptoText.txt", "OpenTextByCryptoText.txt");
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}

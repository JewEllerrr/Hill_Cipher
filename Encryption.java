import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;

public class Encryption {

	private int matrix[][];
	private int module;
	private int dtrmnt;
	private int size;
	private String openText;
	private char[] alphabet = new char[29];

	public Encryption(String key) {

		// проверка ключа на валидность
		double sr = Math.sqrt(key.length());
		if (!((sr - Math.floor(sr)) == 0))
			throw new RuntimeException("Invalid key. The key length must be a square of integer. ");

		int o = 0;
		for (char i = 'a'; i <= 'z'; i++) {
			alphabet[o] = i;
			o++;
		}
		alphabet[alphabet.length - 3] = ',';
		alphabet[alphabet.length - 2] = '.';
		alphabet[alphabet.length - 1] = ' ';
		module = alphabet.length;

		size = (int) Math.sqrt(key.length());

		// сопоставляем ключу его индексы в алфавите
		ArrayDeque<Integer> keyArr = new ArrayDeque<Integer>();
		for (int i = 0; i < key.length(); i++) {
			for (int j = 0; j < alphabet.length; j++) {
				if (key.charAt(i) == alphabet[j])
					keyArr.addLast(new String(alphabet).indexOf(alphabet[j]));
			}
		}

		// заполняем матрицу - ключ
		matrix = new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int k = 0; k < size; k++) {
				if (keyArr.peek() != null)
					matrix[i][k] = (int) keyArr.pop();
			}
		}

		// проверка на обратимость матрицы
		dtrmnt = determinant(matrix) % module;
		if (dtrmnt < 0)
			dtrmnt += module;
		if (dtrmnt == 0 || gcd(dtrmnt, module) != 1) {
			throw new RuntimeException(
					"Invalid key. The determinant of the key matrix does not have an inverse modulo element ");
		}

	}

	public void getResult(String fromPath, String toPath) throws IOException {

		FileWriter writer = new FileWriter(toPath, false);

		List<String> openText = Files.readAllLines(Paths.get(fromPath));
		String cryptoText = null;
		for (String line : openText) {
			line = line.toLowerCase();
			line = line.replaceAll("\n", "");
			cryptoText = getRes(line);
			writer.write(cryptoText + "\n");
			writer.flush();
		}
	}

	private String getRes(String opText) {

		openText = opText;
		// сопоставляем открытому тексту его индексы в алфавите
		ArrayDeque<Integer> openTextArr = new ArrayDeque<Integer>();
		for (int i = 0; i < openText.length(); i++) {
			for (int j = 0; j < alphabet.length; j++) {
				if (openText.charAt(i) == alphabet[j])
					openTextArr.addLast(new String(alphabet).indexOf(alphabet[j]));
			}
		}

		// дополняем текст побелами пока длина открытого текста не кратна размеру
		// матрицы ключа
		if (openText.length() % size != 0) {
			int remainder = 0;
			while ((openText.length() + remainder) % size != 0) {
				remainder++;
			}
			for (int i = 0; i < remainder; i++) {
				openTextArr.addLast(new String(alphabet).indexOf(alphabet[alphabet.length - 1]));
			}
		}

		// алгоритм перемножения векторов(блоков) на матрицу
		int[] result = new int[openTextArr.size()];
		int[] block = null;
		int counter = 0;
		while (openTextArr.peek() != null) {
			block = new int[size];
			int[] vector = new int[size];
			for (int j = 0; j < size; j++) {
				vector[j] = (int) openTextArr.pop();
			}
			for (int m = 0; m < size; m++) {
				for (int k = 0; k < size; k++) {
					block[m] += matrix[k][m] * vector[k];
				}
				result[counter] = block[m] % module;
				counter++;
			}
		}

		// сопоставляем шифртексту его индексы в алфавите
		char[] resulting = new char[result.length];
		for (int i = 0; i < result.length; i++) {
			for (int j = 0; j < alphabet.length; j++) {
				if (result[i] == new String(alphabet).indexOf(alphabet[j]))
					resulting[i] = alphabet[j];
			}
		}
		String resulting2 = String.copyValueOf(resulting);
		return resulting2;
	}

	// вспомогательные функции
	private int gcd(int a, int b) {
		if (b == 0)
			return Math.abs(a);
		return gcd(b, a % b);
	}

	private int determinant(int[][] mat) {
		int result = 0;
		if (mat.length == 1) {
			result = mat[0][0];
			return result;
		}
		if (mat.length == 2) {
			result = mat[0][0] * mat[1][1] - mat[0][1] * mat[1][0];
			return result;
		}
		for (int i = 0; i < mat[0].length; i++) {
			int[][] temp = new int[mat.length - 1][mat[0].length - 1];
			for (int j = 1; j < mat.length; j++) {
				System.arraycopy(mat[j], 0, temp[j - 1], 0, i);
				System.arraycopy(mat[j], i + 1, temp[j - 1], i, mat[0].length - i - 1);
			}
			result += mat[0][i] * Math.pow(-1, i) * determinant(temp);
		}
		return result;
	}

}

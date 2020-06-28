import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.List;

public class Decryption {

	private int matrix[][];
	private int module;
	private int dtrmnt;
	private int size;
	private String cryptoText;
	private String key;
	private char[] alphabet = new char[29];

	public Decryption(String key) {

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

		// находим обратную матрицу
		matrix = inverseMatrix(matrix);

	}

	public void getResult(String Path, String toPath) throws IOException {

		FileWriter writer = new FileWriter(toPath, false);

		List<String> cryptoText = Files.readAllLines(Paths.get(Path));
		String openText = null;
		for (String line : cryptoText) {
			line = line.toLowerCase();
			line = line.replaceAll("\n", "");
			openText = getRes(line);
			writer.write(openText + "\n");
			writer.flush();
		}
	}

	private String getRes(String crText) {

		cryptoText = crText;

		// сопоставляем шифртексту его индексы в алфавите
		ArrayDeque<Integer> cryptoTextArr = new ArrayDeque<Integer>();
		for (int i = 0; i < cryptoText.length(); i++) {
			for (int j = 0; j < alphabet.length; j++) {
				if (cryptoText.charAt(i) == alphabet[j])
					cryptoTextArr.addLast(new String(alphabet).indexOf(alphabet[j]));
			}
		}

		// алгоритм перемножения векторов(блоков) на матрицу
		int[] result = new int[cryptoTextArr.size()];
		int[] block = null;
		int counter = 0;
		while (cryptoTextArr.peek() != null) {
			block = new int[size];
			int[] vector = new int[size];
			for (int j = 0; j < size; j++) {
				vector[j] = (int) cryptoTextArr.pop();
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
		for (int i = 0; i < mat.length; i++) {
			int[][] temp = new int[mat.length - 1][mat[0].length - 1];
			for (int j = 1; j < mat.length; j++) {
				System.arraycopy(mat[j], 0, temp[j - 1], 0, i);
				System.arraycopy(mat[j], i + 1, temp[j - 1], i, mat[0].length - i - 1);
			}
			result += mat[0][i] * Math.pow(-1, i) * determinant(temp);
		}
		return result;
	}

	private int inverseElement(int num) {
		int i;
		for (i = 0; i < module; i++) {
			if ((num * i) % module == 1)
				break;
		}
		return i;
	}

	private int[][] inverseMatrix(int[][] mat) {

		int[][] temp = new int[mat.length - 1][mat.length - 1];
		int[][] res = new int[mat.length][mat.length];
		if (mat.length == 1) {
			res[0][0] = inverseElement(mat[0][0]);
			return res;
		}
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat.length; j++) {
				int n = 0;
				int m = 0;
				for (int k = 0; k < temp.length; k++) {
					for (int l = 0; l < temp.length; l++) {
						if (n == i)
							n++;
						if (m == j)
							m++;
						temp[k][l] = mat[n][m];
						m++;
					}
					n++;
					m = 0;
				}
				res[j][i] = ((int) Math.pow(-1, i + j) * determinant(temp)) % module;
				if (res[j][i] < 0)
					res[j][i] += module;
			}
		}
		int num = inverseElement(dtrmnt);
		for (int i = 0; i < mat.length; i++) {
			for (int j = 0; j < mat.length; j++) {
				res[i][j] = (num * res[i][j]) % module;
			}
		}
		return res;
	}

}

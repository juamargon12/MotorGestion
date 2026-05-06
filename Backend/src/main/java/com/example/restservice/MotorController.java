package com.example.restservice;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Base64;

@RestController
@RequestMapping("/api")
public class MotorController {

	// Configuración común de la base de datos
	private static final String DB_URL = "jdbc:postgresql://localhost:5432/motorgestion";
	private static final String DB_USER = "dit";
	private static final String DB_PASSWORD = "dit";

	// ================== FURGONETAS ==================
	@GetMapping("/furgonetas")
	public ResponseEntity<List<Furgoneta>> getAllFurgonetas() {
		System.out.println(">>> Petición GET recibida en /api/furgonetas");

		List<Furgoneta> furgonetas = new ArrayList<>();
		String sql = "SELECT * FROM furgonetas";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			System.out.println("Conexión a BD establecida. Ejecutando query...");

			while (rs.next()) {
				Furgoneta furgoneta = new Furgoneta();
				// Mapear todos los campos
				furgoneta.setNum(rs.getLong("num"));
				furgoneta.setModelo(rs.getString("modelo"));
				furgoneta.setMatricula(rs.getString("matricula"));
				furgoneta.setCombustible(rs.getString("combustible"));
				furgoneta.setCargaMaxima(rs.getDouble("carga_maxima"));
				furgoneta.setZona(rs.getString("zona"));

				// Convertir bytes a Base64
				byte[] fotoBytes = rs.getBytes("foto");
				if (fotoBytes != null) {
					furgoneta.setFoto(Base64.getEncoder().encodeToString(fotoBytes));
				} else {
					furgoneta.setFoto(null);
				}

				furgonetas.add(furgoneta);
				System.out.println("Furgoneta encontrada: " + furgoneta.getModelo());
			}

			System.out.println("Total de furgonetas recuperadas: " + furgonetas.size());
			return new ResponseEntity<>(furgonetas, HttpStatus.OK);

		} catch (SQLException e) {
			System.err.println("ERROR en getAllFurgonetas:");
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/furgonetas/{num}")
	public ResponseEntity<Furgoneta> getFurgoneta(@PathVariable("num") long num) {
		System.out.println(">>> Petición GET recibida en /api/furgonetas/" + num);
		String sql = "SELECT * FROM furgonetas WHERE num = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				Furgoneta furgoneta = new Furgoneta();
				furgoneta.setNum(rs.getLong("num"));
				furgoneta.setModelo(rs.getString("modelo"));
				furgoneta.setMatricula(rs.getString("matricula"));
				furgoneta.setCombustible(rs.getString("combustible"));
				furgoneta.setCargaMaxima(rs.getDouble("carga_maxima"));
				furgoneta.setZona(rs.getString("zona"));

				byte[] fotoBytes = rs.getBytes("foto");
				if (fotoBytes != null) {
					furgoneta.setFoto(Base64.getEncoder().encodeToString(fotoBytes));
				} else {
					furgoneta.setFoto(null); // CORRECTO: línea válida
				}

				return new ResponseEntity<>(furgoneta, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		} catch (SQLException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/furgonetas/{num}/foto")
	public ResponseEntity<String> actualizarFotoFurgoneta(
			@PathVariable("num") long num,
			@RequestBody String fotoBase64) {

		System.out.println("\n>>> Petición PUT recibida en /api/furgoneta/" + num + "/foto");
		System.out.println(">>> Longitud del Base64 recibido: " + fotoBase64.length() + " caracteres");

		try {
			// Decodificar Base64
			byte[] fotoBytes = Base64.getDecoder().decode(fotoBase64);
			System.out.println(">>> Tamaño de la imagen decodificada: " + fotoBytes.length + " bytes");

			String sql = "UPDATE furgonetas SET foto = ? WHERE num = ?";
			System.out.println(">>> Ejecutando query: " + sql);

			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
					PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setBytes(1, fotoBytes);
				pstmt.setLong(2, num);

				int result = pstmt.executeUpdate();
				System.out.println(">>> Filas afectadas: " + result);

				if (result > 0) {
					System.out.println(">>> Éxito: Foto actualizada para furgoneta " + num);
					return ResponseEntity.ok().body("Foto de la furgoneta " + num + " actualizada correctamente");
				} else {
					System.out.println(">>> Advertencia: No existe furgoneta con num: " + num);
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body("No se encontró ninguna furgoneta con número: " + num);
				}

			} catch (SQLException e) {
				System.err.println(">>> Error SQL al actualizar foto:");
				e.printStackTrace();
				return ResponseEntity.internalServerError()
						.body("Error interno al procesar la solicitud. Detalles: " + e.getMessage());
			}

		} catch (IllegalArgumentException e) {
			System.err.println(">>> Error: Base64 inválido");
			e.printStackTrace();
			return ResponseEntity.badRequest()
					.body("Formato de imagen inválido. Error: " + e.getMessage());
		}
	}

	@PostMapping("/furgonetas")
	public ResponseEntity<Furgoneta> crearFurgoneta(@RequestBody Furgoneta furgoneta) {
		String sqlInsert = "INSERT INTO furgonetas (modelo, matricula, combustible, carga_maxima, zona, foto) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {

			byte[] fotoBytes = furgoneta.getFoto() != null && !furgoneta.getFoto().isEmpty()
					? Base64.getDecoder().decode(furgoneta.getFoto())
					: new byte[0];

			pstmt.setString(1, furgoneta.getModelo());
			pstmt.setString(2, furgoneta.getMatricula());
			pstmt.setString(3, furgoneta.getCombustible());
			pstmt.setDouble(4, furgoneta.getCargaMaxima());
			pstmt.setString(5, furgoneta.getZona());
			pstmt.setBytes(6, fotoBytes);

			int result = pstmt.executeUpdate();

			if (result > 0) {
				// Obtener el ID generado
				ResultSet generatedKeys = pstmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					long idGenerado = generatedKeys.getLong(1);

					// Obtener la furgoneta completa
					String sqlSelect = "SELECT * FROM furgonetas WHERE num = ?";
					try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect)) {
						pstmtSelect.setLong(1, idGenerado);
						ResultSet rs = pstmtSelect.executeQuery();

						if (rs.next()) {
							Furgoneta furgonetaCreada = new Furgoneta();
							furgonetaCreada.setNum(rs.getLong("num"));
							furgonetaCreada.setModelo(rs.getString("modelo"));
							furgonetaCreada.setMatricula(rs.getString("matricula"));
							furgonetaCreada.setCombustible(rs.getString("combustible"));
							furgonetaCreada.setCargaMaxima(rs.getDouble("carga_maxima"));
							furgonetaCreada.setZona(rs.getString("zona"));

							// Convertir bytes a Base64
							byte[] fotoBytesBD = rs.getBytes("foto");
							if (fotoBytesBD != null) {
								furgonetaCreada.setFoto(Base64.getEncoder().encodeToString(fotoBytesBD));
							}

							return new ResponseEntity<>(furgonetaCreada, HttpStatus.CREATED);
						}
					}
				}
				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ================== COCHES ==================
	@GetMapping("/coches")
	public ResponseEntity<List<Coche>> getAllCoches() {
		System.out.println(">>> Petición GET recibida en /api/coches");

		List<Coche> coches = new ArrayList<>();
		String sql = "SELECT * FROM coches";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			System.out.println("Conexión a BD establecida. Ejecutando query...");

			while (rs.next()) {
				Coche coche = new Coche();
				coche.setNum(rs.getLong("num"));
				coche.setModelo(rs.getString("modelo"));
				coche.setNBastidor(rs.getString("n_bastidor"));
				coche.setMatricula(rs.getString("matricula"));
				coche.setAnioFabricacion(rs.getString("anio_fabricacion"));
				coche.setZona(rs.getString("zona"));

				byte[] fotoBytes = rs.getBytes("foto");
				if (fotoBytes != null) {
					coche.setFoto(Base64.getEncoder().encodeToString(fotoBytes));
				} else {
					coche.setFoto(null);
				}

				coches.add(coche);
				System.out.println("Coche encontrado: " + coche.getModelo());
			}

			System.out.println("Total de coches recuperados: " + coches.size());
			return new ResponseEntity<>(coches, HttpStatus.OK);

		} catch (SQLException e) {
			System.err.println("ERROR en getAllCoches:");
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/coches/{num}")
	public ResponseEntity<Coche> getCoche(@PathVariable("num") long num) {
		System.out.println(">>> Petición GET recibida en /api/coches/" + num);
		String sql = "SELECT * FROM coches WHERE num = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				Coche coche = new Coche();
				coche.setNum(rs.getLong("num"));
				coche.setModelo(rs.getString("modelo"));
				coche.setNBastidor(rs.getString("n_bastidor"));
				coche.setMatricula(rs.getString("matricula"));
				coche.setAnioFabricacion(rs.getString("anio_fabricacion"));
				coche.setZona(rs.getString("zona"));

				byte[] fotoBytes = rs.getBytes("foto");
				if (fotoBytes != null) {
					coche.setFoto(Base64.getEncoder().encodeToString(fotoBytes));
				}

				return new ResponseEntity<>(coche, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		} catch (SQLException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/coches/{num}/foto")
	public ResponseEntity<String> actualizarFotoCoche(
			@PathVariable("num") long num,
			@RequestBody String fotoBase64) {

		System.out.println("\n>>> Petición PUT recibida para coche: " + num);

		try {
			byte[] fotoBytes = Base64.getDecoder().decode(fotoBase64);
			String sql = "UPDATE coches SET foto = ? WHERE num = ?";

			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
					PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setBytes(1, fotoBytes);
				pstmt.setLong(2, num);

				int result = pstmt.executeUpdate();

				if (result > 0) {
					return ResponseEntity.ok("Foto del coche " + num + " actualizada");
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body("Coche no encontrado con número: " + num);
				}

			} catch (SQLException e) {
				e.printStackTrace();
				return ResponseEntity.internalServerError()
						.body("Error en la base de datos: " + e.getMessage());
			}

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body("Formato de imagen inválido: " + e.getMessage());
		}
	}

	@PostMapping("/coches")
	public ResponseEntity<Coche> crearCoche(@RequestBody Coche coche) {
		String sql = "INSERT INTO coches (modelo, n_bastidor, matricula, anio_fabricacion, zona, foto) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			byte[] fotoBytes = (coche.getFoto() != null && !coche.getFoto().isEmpty())
					? Base64.getDecoder().decode(coche.getFoto())
					: new byte[0];

			pstmt.setString(1, coche.getModelo());
			pstmt.setString(2, coche.getNBastidor());
			pstmt.setString(3, coche.getMatricula());
			pstmt.setString(4, coche.getAnioFabricacion());
			pstmt.setString(5, coche.getZona());
			pstmt.setBytes(6, fotoBytes);

			int result = pstmt.executeUpdate();

			if (result > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						long idGenerado = generatedKeys.getLong(1);

						String sqlSelect = "SELECT * FROM coches WHERE num = ?";
						try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect)) {
							pstmtSelect.setLong(1, idGenerado);
							ResultSet rs = pstmtSelect.executeQuery();

							if (rs.next()) {
								Coche cocheCreado = new Coche();
								cocheCreado.setNum(rs.getLong("num"));
								cocheCreado.setModelo(rs.getString("modelo"));
								cocheCreado.setNBastidor(rs.getString("n_bastidor"));
								cocheCreado.setMatricula(rs.getString("matricula"));
								cocheCreado.setAnioFabricacion(rs.getString("anio_fabricacion"));
								cocheCreado.setZona(rs.getString("zona"));

								byte[] fotoBD = rs.getBytes("foto");
								cocheCreado.setFoto(fotoBD != null ? Base64.getEncoder().encodeToString(fotoBD) : null);

								return new ResponseEntity<>(cocheCreado, HttpStatus.CREATED);
							}
						}
					}
				}
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ================== MOTOS ==================
	@GetMapping("/motos")
	public ResponseEntity<List<Moto>> getAllMotos() {
		System.out.println(">>> Petición GET recibida en /api/motos");

		List<Moto> motos = new ArrayList<>();
		String sql = "SELECT * FROM motos";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {

			System.out.println("Conexión a BD establecida. Ejecutando query...");

			while (rs.next()) {
				Moto moto = new Moto();
				moto.setNum(rs.getLong("num"));
				moto.setModelo(rs.getString("modelo"));
				moto.setNBastidor(rs.getString("n_bastidor"));
				moto.setMatricula(rs.getString("matricula"));
				moto.setAnioFabricacion(rs.getString("anio_fabricacion"));
				moto.setZona(rs.getString("zona"));

				byte[] fotoBytes = rs.getBytes("foto");
				if (fotoBytes != null) {
					moto.setFoto(Base64.getEncoder().encodeToString(fotoBytes));
				} else {
					moto.setFoto(null);
				}

				motos.add(moto);
				System.out.println("Moto encontrada: " + moto.getModelo());
			}

			System.out.println("Total de motos recuperadas: " + motos.size());
			return new ResponseEntity<>(motos, HttpStatus.OK);

		} catch (SQLException e) {
			System.err.println("ERROR en getAllMotos:");
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/motos/{num}")
	public ResponseEntity<Moto> getMoto(@PathVariable("num") long num) {
		System.out.println(">>> Petición GET recibida en /api/motos/" + num);
		String sql = "SELECT * FROM motos WHERE num = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				Moto moto = new Moto();
				moto.setNum(rs.getLong("num"));
				moto.setModelo(rs.getString("modelo"));
				moto.setNBastidor(rs.getString("n_bastidor"));
				moto.setMatricula(rs.getString("matricula"));
				moto.setAnioFabricacion(rs.getString("anio_fabricacion"));
				moto.setZona(rs.getString("zona"));

				byte[] fotoBytes = rs.getBytes("foto");
				if (fotoBytes != null) {
					moto.setFoto(Base64.getEncoder().encodeToString(fotoBytes));
				}

				return new ResponseEntity<>(moto, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);

		} catch (SQLException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PutMapping("/motos/{num}/foto")
	public ResponseEntity<String> actualizarFotoMoto(
			@PathVariable("num") long num,
			@RequestBody String fotoBase64) {

		System.out.println("\n>>> Petición PUT recibida para moto: " + num);

		try {
			byte[] fotoBytes = Base64.getDecoder().decode(fotoBase64);
			String sql = "UPDATE motos SET foto = ? WHERE num = ?";

			try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
					PreparedStatement pstmt = conn.prepareStatement(sql)) {

				pstmt.setBytes(1, fotoBytes);
				pstmt.setLong(2, num);

				int result = pstmt.executeUpdate();

				if (result > 0) {
					return ResponseEntity.ok("Foto de la moto " + num + " actualizada");
				} else {
					return ResponseEntity.status(HttpStatus.NOT_FOUND)
							.body("Moto no encontrada con número: " + num);
				}

			} catch (SQLException e) {
				e.printStackTrace();
				return ResponseEntity.internalServerError()
						.body("Error en la base de datos: " + e.getMessage());
			}

		} catch (IllegalArgumentException e) {
			return ResponseEntity.badRequest()
					.body("Formato de imagen inválido: " + e.getMessage());
		}
	}

	@PostMapping("/motos")
	public ResponseEntity<Moto> crearMoto(@RequestBody Moto moto) {
		String sql = "INSERT INTO motos (modelo, n_bastidor, matricula, anio_fabricacion, zona, foto) "
				+ "VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			byte[] fotoBytes = (moto.getFoto() != null && !moto.getFoto().isEmpty())
					? Base64.getDecoder().decode(moto.getFoto())
					: new byte[0];

			pstmt.setString(1, moto.getModelo());
			pstmt.setString(2, moto.getNBastidor());
			pstmt.setString(3, moto.getMatricula());
			pstmt.setString(4, moto.getAnioFabricacion());
			pstmt.setString(5, moto.getZona());
			pstmt.setBytes(6, fotoBytes);

			int result = pstmt.executeUpdate();

			if (result > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						long idGenerado = generatedKeys.getLong(1);

						String sqlSelect = "SELECT * FROM motos WHERE num = ?";
						try (PreparedStatement pstmtSelect = conn.prepareStatement(sqlSelect)) {
							pstmtSelect.setLong(1, idGenerado);
							ResultSet rs = pstmtSelect.executeQuery();

							if (rs.next()) {
								Moto motoCreada = new Moto();
								motoCreada.setNum(rs.getLong("num"));
								motoCreada.setModelo(rs.getString("modelo"));
								motoCreada.setNBastidor(rs.getString("n_bastidor"));
								motoCreada.setMatricula(rs.getString("matricula"));
								motoCreada.setAnioFabricacion(rs.getString("anio_fabricacion"));
								motoCreada.setZona(rs.getString("zona"));

								byte[] fotoBD = rs.getBytes("foto");
								motoCreada.setFoto(fotoBD != null ? Base64.getEncoder().encodeToString(fotoBD) : null);

								return new ResponseEntity<>(motoCreada, HttpStatus.CREATED);
							}
						}
					}
				}
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/furgonetas/{num}")
	public ResponseEntity<String> eliminarFurgoneta(@PathVariable("num") long num) {
		System.out.println(">>> Petición DELETE recibida en /api/furgonetas/" + num);
		String sql = "DELETE FROM furgonetas WHERE num = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			int result = pstmt.executeUpdate();

			if (result > 0) {
				return ResponseEntity.ok("Furgoneta eliminada");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No se encontró la furgoneta con número: " + num);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
					.body("Error en la base de datos: " + e.getMessage());
		}
	}

	@DeleteMapping("/coches/{num}")
	public ResponseEntity<String> eliminarCoche(@PathVariable("num") long num) {
		System.out.println(">>> Petición DELETE recibida en /api/coches/" + num);
		String sql = "DELETE FROM coches WHERE num = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			int result = pstmt.executeUpdate();

			if (result > 0) {
				return ResponseEntity.ok("Coche eliminado");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No se encontró el coche con número: " + num);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
					.body("Error en la base de datos: " + e.getMessage());
		}
	}

	@DeleteMapping("/motos/{num}")
	public ResponseEntity<String> eliminarMoto(@PathVariable("num") long num) {
		System.out.println(">>> Petición DELETE recibida en /api/motos/" + num);
		String sql = "DELETE FROM motos WHERE num = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			int result = pstmt.executeUpdate();

			if (result > 0) {
				return ResponseEntity.ok("Moto eliminada");
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND)
						.body("No se encontró la moto con número: " + num);
			}

		} catch (SQLException e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError()
					.body("Error en la base de datos: " + e.getMessage());
		}
	}

	// ================== MANTENIMIENTOS ==================
	@GetMapping("/mantenimientos")
	public ResponseEntity<List<Mantenimiento>> getAllMantenimientos() {
		List<Mantenimiento> mants = new ArrayList<>();
		String sql = "SELECT * FROM mantenimientos ORDER BY num ASC";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while (rs.next()) {
				Mantenimiento mant = new Mantenimiento();
				mant.setNum(rs.getLong("num"));
				mant.setTexto(rs.getString("texto"));
				mant.setRealizada(rs.getBoolean("realizada"));
				mant.setTipoVehiculo(rs.getString("tipo_vehiculo"));
				long vNum = rs.getLong("vehiculo_num");
				if (!rs.wasNull()) mant.setVehiculoNum(vNum);
				mants.add(mant);
			}
			return new ResponseEntity<>(mants, HttpStatus.OK);
		} catch (SQLException e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@PostMapping("/mantenimientos")
	public ResponseEntity<Mantenimiento> crearMantenimiento(@RequestBody Mantenimiento mant) {
		String sql = "INSERT INTO mantenimientos (texto, realizada, tipo_vehiculo, vehiculo_num) VALUES (?, ?, ?, ?)";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			pstmt.setString(1, mant.getTexto());
			pstmt.setBoolean(2, mant.isRealizada());
			if (mant.getTipoVehiculo() != null && !mant.getTipoVehiculo().isEmpty()) {
				pstmt.setString(3, mant.getTipoVehiculo());
			} else {
				pstmt.setNull(3, java.sql.Types.VARCHAR);
			}
			if (mant.getVehiculoNum() != null) {
				pstmt.setLong(4, mant.getVehiculoNum());
			} else {
				pstmt.setNull(4, java.sql.Types.BIGINT);
			}
			int result = pstmt.executeUpdate();

			if (result > 0) {
				try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						mant.setNum(generatedKeys.getLong(1));
						return new ResponseEntity<>(mant, HttpStatus.CREATED);
					}
				}
			}
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		} catch (SQLException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@DeleteMapping("/mantenimientos/{num}")
	public ResponseEntity<String> eliminarMantenimiento(@PathVariable("num") long num) {
		System.out.println(">>> Petición DELETE recibida en /api/mantenimientos/" + num);
		String sql = "DELETE FROM mantenimientos WHERE num = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setLong(1, num);
			int result = pstmt.executeUpdate();
			if (result > 0) {
				return ResponseEntity.ok("Mantenimiento eliminado");
			}
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No encontrado");
		} catch (SQLException e) {
			return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
		}
	}

	// ================== PUT GENERAL — Actualizar Coche ==================
	@PutMapping("/coches/{num}")
	public ResponseEntity<Coche> actualizarCoche(@PathVariable("num") long num, @RequestBody Coche coche) {
		System.out.println(">>> Petición PUT recibida en /api/coches/" + num);
		String sql = "UPDATE coches SET modelo = ?, n_bastidor = ?, matricula = ?, anio_fabricacion = ?, zona = ?, foto = ? WHERE num = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, coche.getModelo());
			pstmt.setString(2, coche.getNBastidor());
			pstmt.setString(3, coche.getMatricula());
			pstmt.setString(4, coche.getAnioFabricacion());
			pstmt.setString(5, coche.getZona());

			byte[] fotoBytes = (coche.getFoto() != null && !coche.getFoto().isEmpty())
					? Base64.getDecoder().decode(coche.getFoto())
					: null;
			if (fotoBytes != null) {
				pstmt.setBytes(6, fotoBytes);
			} else {
				pstmt.setNull(6, java.sql.Types.BINARY);
			}
			pstmt.setLong(7, num);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				coche.setNum(num);
				return new ResponseEntity<>(coche, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ================== PUT GENERAL — Actualizar Moto ==================
	@PutMapping("/motos/{num}")
	public ResponseEntity<Moto> actualizarMoto(@PathVariable("num") long num, @RequestBody Moto moto) {
		System.out.println(">>> Petición PUT recibida en /api/motos/" + num);
		String sql = "UPDATE motos SET modelo = ?, n_bastidor = ?, matricula = ?, anio_fabricacion = ?, zona = ?, foto = ? WHERE num = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, moto.getModelo());
			pstmt.setString(2, moto.getNBastidor());
			pstmt.setString(3, moto.getMatricula());
			pstmt.setString(4, moto.getAnioFabricacion());
			pstmt.setString(5, moto.getZona());

			byte[] fotoBytes = (moto.getFoto() != null && !moto.getFoto().isEmpty())
					? Base64.getDecoder().decode(moto.getFoto())
					: null;
			if (fotoBytes != null) {
				pstmt.setBytes(6, fotoBytes);
			} else {
				pstmt.setNull(6, java.sql.Types.BINARY);
			}
			pstmt.setLong(7, num);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				moto.setNum(num);
				return new ResponseEntity<>(moto, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ================== PUT GENERAL — Actualizar Furgoneta ==================
	@PutMapping("/furgonetas/{num}")
	public ResponseEntity<Furgoneta> actualizarFurgoneta(@PathVariable("num") long num,
			@RequestBody Furgoneta furgoneta) {
		System.out.println(">>> Petición PUT recibida en /api/furgonetas/" + num);
		String sql = "UPDATE furgonetas SET modelo = ?, matricula = ?, combustible = ?, carga_maxima = ?, zona = ?, foto = ? WHERE num = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, furgoneta.getModelo());
			pstmt.setString(2, furgoneta.getMatricula());
			pstmt.setString(3, furgoneta.getCombustible());
			pstmt.setDouble(4, furgoneta.getCargaMaxima());
			pstmt.setString(5, furgoneta.getZona());

			byte[] fotoBytes = (furgoneta.getFoto() != null && !furgoneta.getFoto().isEmpty())
					? Base64.getDecoder().decode(furgoneta.getFoto())
					: null;
			if (fotoBytes != null) {
				pstmt.setBytes(6, fotoBytes);
			} else {
				pstmt.setNull(6, java.sql.Types.BINARY);
			}
			pstmt.setLong(7, num);

			int result = pstmt.executeUpdate();
			if (result > 0) {
				furgoneta.setNum(num);
				return new ResponseEntity<>(furgoneta, HttpStatus.OK);
			}
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (SQLException | IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ================== PUT — Toggle Mantenimiento (marcar/desmarcar)
	// ==================
	@PutMapping("/mantenimientos/{num}")
	public ResponseEntity<Mantenimiento> toggleMantenimiento(@PathVariable("num") long num) {
		System.out.println(">>> Petición PUT (toggle) recibida en /api/mantenimientos/" + num);
		String sqlGet = "SELECT * FROM mantenimientos WHERE num = ?";
		String sqlUpdate = "UPDATE mantenimientos SET realizada = ? WHERE num = ?";
		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
				PreparedStatement pstmtGet = conn.prepareStatement(sqlGet)) {

			pstmtGet.setLong(1, num);
			ResultSet rs = pstmtGet.executeQuery();

			if (!rs.next()) {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
			boolean estadoActual = rs.getBoolean("realizada");
			boolean nuevoEstado = !estadoActual;
			String tipoVehiculo = rs.getString("tipo_vehiculo");
			long vNum = rs.getLong("vehiculo_num");
			boolean vNumNull = rs.wasNull();

			try (PreparedStatement pstmtUpd = conn.prepareStatement(sqlUpdate)) {
				pstmtUpd.setBoolean(1, nuevoEstado);
				pstmtUpd.setLong(2, num);
				pstmtUpd.executeUpdate();
			}

			Mantenimiento mant = new Mantenimiento();
			mant.setNum(num);
			mant.setRealizada(nuevoEstado);
			mant.setTipoVehiculo(tipoVehiculo);
			if (!vNumNull) mant.setVehiculoNum(vNum);
			return new ResponseEntity<>(mant, HttpStatus.OK);

		} catch (SQLException e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	// ================== LOGIN (Tema 02 — Autenticación y Roles) ==================

	// Clase interna para deserializar el JSON del login
	public static class LoginRequest {
		private String usuario;
		private String password;
		public String getUsuario() { return usuario; }
		public void setUsuario(String usuario) { this.usuario = usuario; }
		public String getPassword() { return password; }
		public void setPassword(String password) { this.password = password; }
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
		System.out.println(">>> Petición POST recibida en /api/login para usuario: " + loginRequest.getUsuario());
		String sql = "SELECT rol FROM usuarios WHERE usuario = ? AND password = ?";

		try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			 PreparedStatement pstmt = conn.prepareStatement(sql)) {

			pstmt.setString(1, loginRequest.getUsuario());
			pstmt.setString(2, loginRequest.getPassword());
			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				String rol = rs.getString("rol");
				System.out.println(">>> Login correcto. Rol: " + rol);
				return new ResponseEntity<>(rol, HttpStatus.OK); // Devuelve "JEFE" o "EMPLEADO"
			}
			System.out.println(">>> Login fallido: credenciales incorrectas");
			return new ResponseEntity<>("Credenciales inválidas", HttpStatus.UNAUTHORIZED);

		} catch (SQLException e) {
			e.printStackTrace();
			return new ResponseEntity<>("Error de servidor", HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}

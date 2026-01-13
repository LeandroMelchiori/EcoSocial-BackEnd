import csv

from pathlib import Path

BASE = Path(__file__).resolve().parent.parent
INFILE = BASE / "tools" / "localidades_sf.csv"
OUTFILE = BASE / "src" / "main" / "resources" / "db" / "migration" / "V7_1__seed_localidades_santa_fe.sql"

def esc(s):
    if s is None:
        return "NULL"
    s = str(s).strip().replace("'", "''")
    return f"'{s}'" if s else "NULL"

with open(INFILE, newline="", encoding="utf-8") as f:
    r = csv.DictReader(f)
    print("Headers:", r.fieldnames)

with open(INFILE, newline="", encoding="utf-8") as f, open(OUTFILE, "w", encoding="utf-8") as out:
    r = csv.DictReader(f)

    out.write("-- Seed localidades Santa Fe (Georef)\n")
    out.write("TRUNCATE TABLE localidad;\n")

    count = 0
    for row in r:
        georef_id = row.get("localidad_id")
        nombre = row.get("localidad_nombre")
        depto = row.get("departamento_nombre") or row.get("departamento")
        lat = row.get("localidad_centroide_lat")
        lon = row.get("localidad_centroide_lon")

        if not georef_id or not nombre:
            continue

        latv = "NULL" if not lat else str(lat)
        lonv = "NULL" if not lon else str(lon)

        out.write(
            "INSERT INTO localidad (georef_id, nombre, departamento, lat, lon, activo) VALUES "
            f"({esc(georef_id)}, {esc(nombre)}, {esc(depto)}, {latv}, {lonv}, 1);\n"
        )
        count += 1
print("INSERTs generados:", count)

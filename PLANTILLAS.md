# 📧📄 Guía de Plantillas HTML - VetClinic Pro

Este documento describe dónde están ubicadas todas las plantillas HTML que puedes modificar para personalizar el diseño de correos electrónicos y PDFs.

---

## 📧 PLANTILLAS DE CORREOS ELECTRÓNICOS (Backend)

**Ubicación:** `src/main/resources/templates/emails/`

### Archivos disponibles:

1. **`base.html`** - Plantilla base para todos los correos
   - Contiene el header, footer y estructura común
   - Variables disponibles: `{{title}}`, `{{preheader}}`, `{{content}}`, `{{clinicName}}`, `{{currentYear}}`, `{{frontendUrl}}`

2. **`appointment-created.html`** - Cita creada
   - Variables: `{{ownerName}}`, `{{patientName}}`, `{{scheduledDate}}`, `{{appointmentType}}`, `{{veterinarianName}}`, `{{frontendUrl}}`

3. **`appointment-confirmed.html`** - Cita confirmada
   - Variables similares a appointment-created.html

4. **`appointment-cancelled.html`** - Cita cancelada
   - Variables similares a appointment-created.html

5. **`appointment-completed.html`** - Cita completada
   - Variables similares a appointment-created.html

6. **`appointment-reminder.html`** - Recordatorio de cita
   - Variables similares a appointment-created.html

7. **`appointment-status-changed.html`** - Cambio de estado de cita
   - Variables similares + `{{previousStatus}}`, `{{newStatus}}`

8. **`welcome.html`** - Bienvenida a usuarios
   - Variables: `{{fullName}}`, `{{username}}`, `{{email}}`, `{{loginUrl}}`

9. **`owner-welcome.html`** - Bienvenida a propietarios
   - Variables: `{{fullName}}`, `{{username}}`, `{{password}}`, `{{loginUrl}}`

10. **`password-reset.html`** - Restablecimiento de contraseña
    - Variables: `{{fullName}}`, `{{resetLink}}`

11. **`password-changed.html`** - Confirmación de cambio de contraseña
    - Variables: `{{fullName}}`

---

## 📄 PLANTILLAS DE PDF (Frontend)

### 1. Historias Clínicas

**Ubicación:** `frontend-vetclinio-1/src/components/medical-records/MedicalRecordDetailsDialog.tsx`

**Funciones:**
- **`generatePDFHTML()`** (línea ~492) - Plantilla HTML inline para PDF
- **`generatePDFFullHTML()`** (línea ~318) - Versión completa con etiquetas HTML

**Plantilla de referencia creada:** `frontend-vetclinio-1/src/templates/medical-record-pdf.html`
- Esta es una plantilla de referencia que puedes usar como guía
- Las variables están marcadas con `{{variableName}}` (debes reemplazarlas en el código)

**Variables disponibles:**
- `{{patientName}}` - Nombre del paciente
- `{{id}}` - ID de la consulta
- `{{recordDate}}` - Fecha del registro
- `{{recordTime}}` - Hora del registro
- `{{weight}}` - Peso (ej: "25 kg" o "--")
- `{{temperature}}` - Temperatura (ej: "38.5°C" o "--")
- `{{heartRate}}` - Frecuencia cardíaca (ej: "120 bpm" o "--")
- `{{symptoms}}` - Síntomas reportados (opcional)
- `{{diagnosis}}` - Diagnóstico médico
- `{{treatment}}` - Plan de tratamiento
- `{{notes}}` - Notas clínicas (opcional)
- `{{veterinarianName}}` - Nombre del veterinario
- `{{followUpDate}}` - Próxima visita (opcional)
- `{{followUpRequired}}` - Boolean para mostrar badge
- `{{generatedDate}}` - Fecha de generación del PDF

### 2. Prescripciones

**Ubicación Backend:** `src/main/java/com/vetclinic/patterns/factory/export/PdfDocumentExporter.java`
- Generación programática con iTextPDF (no usa plantillas HTML)
- Para modificar el diseño, edita este archivo Java

---

## 🎨 CÓMO MODIFICAR LAS PLANTILLAS

### Para Correos Electrónicos:

1. Edita los archivos en `src/main/resources/templates/emails/`
2. Usa variables con formato `{{variableName}}`
3. Los estilos deben ser inline (dentro de atributos `style="..."`)
4. Usa tablas HTML para estructura (mejor compatibilidad con clientes de correo)

### Para PDFs de Historias Clínicas:

1. Edita la función `generatePDFHTML()` en `MedicalRecordDetailsDialog.tsx`
2. O usa la plantilla de referencia en `frontend-vetclinio-1/src/templates/medical-record-pdf.html`
3. Los estilos pueden ser inline o en una sección `<style>`
4. Usa variables JavaScript template literals: `${variable}`

---

## 📝 NOTAS IMPORTANTES

- **Correos:** Deben usar estilos inline para máxima compatibilidad
- **PDFs:** Pueden usar CSS más avanzado ya que se renderizan en el navegador
- **Colores principales del proyecto:** 
  - Primario: `#4F46E5` (indigo)
  - Secundario: `#7C3AED` (violet)
  - Éxito: `#10B981` (emerald)
  - Advertencia: `#F59E0B` (amber)

---

## 🔧 FUNCIÓN CENTRALIZADA DE DESCARGA

La función `downloadPDFFromHTML()` en `MedicalRecordDetailsDialog.tsx` (línea ~236) es la función centralizada que:
- Recibe HTML como string
- Recibe el nombre del archivo
- Genera y descarga el PDF

Puedes reutilizar esta función para otros documentos cambiando solo la plantilla HTML.


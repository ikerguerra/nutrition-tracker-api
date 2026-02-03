# Configuración de Google OAuth2

Para solucionar el error `401: invalid_client`, necesitas crear credenciales válidas en Google Cloud Platform. Sigue estos pasos:

## 1. Crear Proyecto en Google Cloud
1. Ve a [Google Cloud Console](https://console.cloud.google.com/).
2. Crea un nuevo proyecto (o selecciona uno existente).

## 2. Configurar Pantalla de Consentimiento
1. En el menú lateral, ve a **APIs & Services** > **OAuth consent screen**.
2. Selecciona **External** (para pruebas personales) y haz clic en **Create**.
3. Rellena los campos obligatorios:
   - **App name**: Nutrition Tracker
   - **User support email**: Tu email.
   - **Developer contact information**: Tu email.
4. Haz clic en **Save and Continue** (puedes omitir Scopes y Test Users por ahora, o añadir tu propio email como Test User si la app está en modo "Testing").

## 3. Crear Credenciales
1. Ve a **APIs & Services** > **Credentials**.
2. Haz clic en **+ CREATE CREDENTIALS** > **OAuth client ID**.
3. **Application type**: Web application.
4. **Name**: Nutrition Tracker Local.
5. **Authorized redirect URIs**:
   - Agrega la siguiente URL (basada en tu configuración de Spring Boot):
   - `http://localhost:8080/login/oauth2/code/google`
   > **Nota**: Si cambias el puerto o el path, deberás actualizar esto.

6. Haz clic en **Create**.

## 4. Copiar Claves
Aparecerá un pop-up con tus credenciales. Copia:
- **Client ID**
- **Client Secret**

## 5. Configurar la Aplicación
Pega estos valores en el archivo `src/main/resources/application-local.yml` (que se creará a continuación).

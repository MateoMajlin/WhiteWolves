package winterwolves.personajes.habilidadesGuerrero;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import java.util.HashMap;
import java.util.Map;

public class Arma {

    protected float tiempoDesdeUltimoGolpe = 0f;
    protected float cooldown = 1f;

    protected Animation<TextureRegion> animacion;
    protected float stateTime;
    protected boolean activo;
    protected Texture hoja;

    protected World world;
    protected Body body;
    protected float ppm;

    protected float daño;
    protected float multiplicadorDaño = 1f; // NUEVO: multiplicador temporal

    protected Map<Direccion, HitboxConfig> hitboxes = new HashMap<>();

    protected static class HitboxConfig {
        float ancho, alto, offsetX, offsetY, angleDeg;
        HitboxConfig(float ancho, float alto, float offsetX, float offsetY, float angleDeg) {
            this.ancho = ancho;
            this.alto = alto;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
            this.angleDeg = angleDeg;
        }
    }

    public Arma(World world, float ppm) {
        this.world = world;
        this.ppm = ppm;
        this.stateTime = 0;
        this.activo = false;
    }

    public void activar(float x, float y, Direccion dir) {
        if (!activo && puedeAtacar()) {
            activo = true;
            stateTime = 0;
            tiempoDesdeUltimoGolpe = 0f;

            HitboxConfig cfg = hitboxes.get(dir);
            if (cfg == null) return;

            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.position.set(x/ppm, y/ppm);
            body = world.createBody(bodyDef);

            PolygonShape shape = new PolygonShape();
            Vector2 offset = new Vector2(cfg.offsetX/ppm, cfg.offsetY/ppm);
            shape.setAsBox(cfg.ancho/2/ppm, cfg.alto/2/ppm, offset, (float)Math.toRadians(cfg.angleDeg));

            FixtureDef fixtureDef = new FixtureDef();
            fixtureDef.shape = shape;
            fixtureDef.isSensor = true;
            body.createFixture(fixtureDef).setUserData(this);

            shape.dispose();
            body.setUserData(this);
        }
    }

    public void update(float delta, float x, float y) {
        tiempoDesdeUltimoGolpe += delta;
        if (activo) {
            stateTime += delta;
            if (body != null) body.setTransform(x/ppm, y/ppm, 0);
            if (animacion.isAnimationFinished(stateTime)) {
                activo = false;
                if (body != null) {
                    world.destroyBody(body);
                    body = null;
                }
                multiplicadorDaño = 1f; // Resetear daño al terminar golpe
            }
        }
    }

    public void draw(Batch batch, float x, float y, float width, float height, float angle) {
        if (activo && animacion != null) {
            TextureRegion frame = animacion.getKeyFrame(stateTime);
            batch.draw(frame, x, y, width/2, height/2, width, height, 1, 1, angle);
        }
    }

    public void dispose() {
        if (hoja != null) hoja.dispose();
    }

    public boolean puedeAtacar() {
        return tiempoDesdeUltimoGolpe >= cooldown;
    }

    public float getCooldownProgreso() {
        return Math.min(tiempoDesdeUltimoGolpe / cooldown, 1f);
    }

    public boolean isActivo() {
        return activo;
    }

    public float getDañoReal() {
        return daño * multiplicadorDaño;
    }

    public void setMultiplicadorDaño(float valor) {
        multiplicadorDaño = valor;
    }

    public enum Direccion {
        UP, DOWN, LEFT, RIGHT,
        UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
    }

    public static Direccion vectorADireccion(Vector2 dir) {
        float angle = (float) Math.toDegrees(Math.atan2(dir.y, dir.x));

        if (angle >= -22.5 && angle < 22.5) return Direccion.RIGHT;
        if (angle >= 22.5 && angle < 67.5) return Direccion.UP_RIGHT;
        if (angle >= 67.5 && angle < 112.5) return Direccion.UP;
        if (angle >= 112.5 && angle < 157.5) return Direccion.UP_LEFT;
        if (angle >= -67.5 && angle < -22.5) return Direccion.DOWN_RIGHT;
        if (angle >= -112.5 && angle < -67.5) return Direccion.DOWN;
        if (angle >= -157.5 && angle < -112.5) return Direccion.DOWN_LEFT;
        return Direccion.LEFT;
    }
}

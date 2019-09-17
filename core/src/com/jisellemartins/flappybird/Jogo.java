package com.jisellemartins.flappybird;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Jogo extends ApplicationAdapter {
    private SpriteBatch batch; // Responsável por exibir as imagens dentro do jogo
    private Texture[] passaros; // Textura
    private Texture fundo;
    private Texture canoBaixo;
    private Texture canoTopo;
    private Texture gameOver;

    private ShapeRenderer shapeRenderer;
    private Circle circuloPassaro;
    private Rectangle retanguloCanoTopo;
    private Rectangle retanguloCanoBaixo;

    private float larguraDispositivo;
    private float alturaDispositivo;
    private float variacao = 0;
    private float gravidade = 2;
    private float posicaoInicialVerticalPassaro = 0;
    private float posicaoCanoHorizontal;
    private float posicaoCanoVertical;
    private float espacoEntreCanos;
    private Random random;
    private int pontos = 0;
    private int pontuacaoMaxima = 0;
    private int estadoJogo = 0;
    private BitmapFont textoPontuacao;
    private BitmapFont textoReiniciar;
    private BitmapFont textoMelhorPontuação;

    private Sound somVoando;
    private Sound somColisao;
    private Sound somPontuacao;

    private Preferences preferencias;
    private static final String PREFERENCIAS = "pontuacaoMaxima";
    private OrthographicCamera camera;
    private Viewport viewport;
    private final float VIRTUAL_WIDTH = 720;
    private final float VIRTUAL_HEIGHT = 1280;

    @Override
    public void create() {
        inicializarTexturas();
        inicializarObjetos();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        verificarEstadoJogo();
        baterAsas();
        desenharTexturas();
        detectarColisoes();
    }

    private void detectarColisoes() {
        circuloPassaro.set(30 + passaros[0].getWidth() / 2, posicaoInicialVerticalPassaro +
                passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);
        retanguloCanoTopo.set(posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 +
                posicaoCanoVertical, canoTopo.getWidth(), canoTopo.getHeight());
        retanguloCanoBaixo.set(posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() -
                espacoEntreCanos / 2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight());
        if (Intersector.overlaps(circuloPassaro, retanguloCanoBaixo) || Intersector.overlaps(circuloPassaro, retanguloCanoTopo)) {
            if (estadoJogo == 1) {
                somColisao.play();
                estadoJogo = 2;
            }
        }


       /* shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.circle(30 + passaros[0].getWidth() / 2, posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2, passaros[0].getWidth() / 2);
        shapeRenderer.rect(posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical, canoTopo.getWidth(), canoTopo.getHeight());
        shapeRenderer.rect(posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical, canoBaixo.getWidth(), canoBaixo.getHeight());
        shapeRenderer.end();*/
    }

    private void verificarEstadoJogo() {
        boolean toqueTela = Gdx.input.justTouched();
        if (estadoJogo == 0) {
            if (toqueTela) {
                gravidade = -10;
                estadoJogo = 1;
                somVoando.play();
            }

        } else if (estadoJogo == 1) {
            if (toqueTela) {
                gravidade = -10;
                somVoando.play();
            }

            posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200;
            if (posicaoCanoHorizontal < -canoTopo.getWidth()) {
                posicaoCanoHorizontal = larguraDispositivo;
                posicaoCanoVertical = random.nextInt(800) - 400;
                pontos++;
                somPontuacao.play();
            }
            if (posicaoInicialVerticalPassaro > 0 || toqueTela)
                posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
            posicaoCanoHorizontal--;
            gravidade++;
        } else if (estadoJogo == 2) {

            if (pontos > pontuacaoMaxima) {
                pontuacaoMaxima = pontos;
                preferencias.putInteger(PREFERENCIAS, pontuacaoMaxima);
            }

            if (posicaoInicialVerticalPassaro > 0 || toqueTela)
                posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
            gravidade++;
            if (toqueTela) {
                estadoJogo = 0;
                pontos = 0;
                gravidade = 0;
                posicaoInicialVerticalPassaro = alturaDispositivo / 2;
                posicaoCanoHorizontal = larguraDispositivo;
            }
        }


    }

    public void baterAsas() {
        variacao += Gdx.graphics.getDeltaTime() * 10;
        if (variacao > 3) variacao = 0;
    }

    private void desenharTexturas() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
        batch.draw(passaros[(int) variacao], 30, posicaoInicialVerticalPassaro);
        batch.draw(canoBaixo, posicaoCanoHorizontal, alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
        batch.draw(canoTopo, posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical);
        textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2, alturaDispositivo - 110);
        if (estadoJogo == 2) {
            batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2, alturaDispositivo / 2);
            textoReiniciar.draw(batch, "Toque para reiniciar!", larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight() / 2);
            textoMelhorPontuação.draw(batch, "Seu recorde é: " + pontuacaoMaxima + " pontos", larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
        }
        batch.end();
    }

    public void inicializarTexturas() {
        passaros = new Texture[3];
        passaros[0] = new Texture("passaro1.png");
        passaros[1] = new Texture("passaro2.png");
        passaros[2] = new Texture("passaro3.png");
        fundo = new Texture("fundo.png");
        canoBaixo = new Texture("cano_baixo_maior.png");
        canoTopo = new Texture("cano_topo_maior.png");
        gameOver = new Texture("game_over.png");
    }

    public void inicializarObjetos() {
        batch = new SpriteBatch();
        random = new Random();
        larguraDispositivo = VIRTUAL_WIDTH;
        alturaDispositivo = VIRTUAL_HEIGHT;
        posicaoInicialVerticalPassaro = alturaDispositivo / 2;
        posicaoCanoHorizontal = larguraDispositivo;
        espacoEntreCanos = 200;
        textoPontuacao = new BitmapFont();
        textoPontuacao.setColor(Color.BLACK);
        textoPontuacao.getData().setScale(5);

        textoReiniciar = new BitmapFont();
        textoReiniciar.setColor(Color.GREEN);
        textoReiniciar.getData().setScale(2);

        textoMelhorPontuação = new BitmapFont();
        textoMelhorPontuação.setColor(Color.RED);
        textoMelhorPontuação.getData().setScale(2);

        shapeRenderer = new ShapeRenderer();
        circuloPassaro = new Circle();
        retanguloCanoBaixo = new Rectangle();
        retanguloCanoTopo = new Rectangle();
        shapeRenderer.setColor(Color.PINK);

        somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
        somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
        somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

        preferencias = Gdx.app.getPreferences("flappyBird");
        pontuacaoMaxima = preferencias.getInteger(PREFERENCIAS, 0);
        camera = new OrthographicCamera();
        camera.position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
        viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_WIDTH, camera);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void dispose() {
    }
}

package com.thorschmidt.curso.instagram.model;

import com.google.firebase.database.DatabaseReference;
import com.thorschmidt.curso.instagram.util.ConfiguracaoFirebase;

public class Comentario {

    private String idComentario;
    private String idPostagem;
    private String idUsuario;
    private String caminhoFoto;
    private String nomeUsuario;
    private String comentario;

    public Comentario() {
    }

    public boolean salvar(){

        /*
        + Comentarios
            + id_postagem
                + id_comentario
                    comentario
        * */
        DatabaseReference comentariosRef = ConfiguracaoFirebase.getFirebase()
                .child("comentarios")
                .child( getIdPostagem() );

        String chaveComentario = comentariosRef.push().getKey();
        setIdComentario( chaveComentario );
        comentariosRef.child( getIdComentario() ).setValue( this );

        return true;
    }

    private String getIdComentario() {
        return idComentario;
    }

    private void setIdComentario(String idComentario) {
        this.idComentario = idComentario;
    }

    private String getIdPostagem() {
        return idPostagem;
    }

    public void setIdPostagem(String idPostagem) {
        this.idPostagem = idPostagem;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getCaminhoFoto() {
        return caminhoFoto;
    }

    public void setCaminhoFoto(String caminhoFoto) {
        this.caminhoFoto = caminhoFoto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public void setNomeUsuario(String nomeUsuario) {
        this.nomeUsuario = nomeUsuario;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
}
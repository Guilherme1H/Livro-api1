DELETE FROM Livro_Autor;
DELETE FROM Livro;
DELETE FROM DetalhesEditora;
DELETE FROM Editora;
DELETE FROM Autor;

INSERT INTO Editora (id, nome, endereco) VALUES (1, 'Companhia das Letras', 'Rua Bandeira Paulista, 702 - São Paulo');
INSERT INTO DetalhesEditora (id, telefone, email, editora_id) VALUES (1, '(11) 3707-3500', 'contato@companhiadasletras.com.br', 1);

INSERT INTO Editora (id, nome, endereco) VALUES (2, 'Editora Rocco', 'Rua Prof. Alfredo Gomes, 37 - Rio de Janeiro');
INSERT INTO DetalhesEditora (id, telefone, email, editora_id) VALUES (2, '(21) 3525-2000', 'sac@rocco.com.br', 2);

INSERT INTO Editora (id, nome, endereco) VALUES (3, 'Editora Record', 'Rua Argentina, 171 - Rio de Janeiro');
INSERT INTO DetalhesEditora (id, telefone, email, editora_id) VALUES (3, '(21) 2585-2000', 'atendimento@record.com.br', 3);

INSERT INTO Editora (id, nome, endereco) VALUES (4, 'Nova Fronteira', 'Rua Nova York, 123 - Rio de Janeiro');
INSERT INTO DetalhesEditora (id, telefone, email, editora_id) VALUES (4, '(21) 2222-1111', 'contato@novafronteira.com.br', 4);

INSERT INTO Autor (id, nome, nacionalidade) VALUES (1, 'Machado de Assis', 'Brasileira');
INSERT INTO Autor (id, nome, nacionalidade) VALUES (2, 'Paulo Coelho', 'Brasileira');
INSERT INTO Autor (id, nome, nacionalidade) VALUES (3, 'Jorge Amado', 'Brasileira');
INSERT INTO Autor (id, nome, nacionalidade) VALUES (4, 'João Guimarães Rosa', 'Brasileira');
INSERT INTO Autor (id, nome, nacionalidade) VALUES (5, 'J.R.R. Tolkien', 'Britânica');

INSERT INTO Livro (id, titulo, isbn, anoPublicacao, status, editora_id) VALUES (1, 'Dom Casmurro', '9788535907408', 1899, 'DISPONIVEL', 1);
INSERT INTO Livro (id, titulo, isbn, anoPublicacao, status, editora_id) VALUES (2, 'O Alquimista', '9788532522328', 1988, 'DISPONIVEL', 2);
INSERT INTO Livro (id, titulo, isbn, anoPublicacao, status, editora_id) VALUES (3, 'Capitães da Areia', '9788535900591', 1937, 'EMPRESTADO', 3);
INSERT INTO Livro (id, titulo, isbn, anoPublicacao, status, editora_id) VALUES (4, 'Grande Sertão: Veredas', '9788520921934', 1956, 'DISPONIVEL', 4);
INSERT INTO Livro (id, titulo, isbn, anoPublicacao, status, editora_id) VALUES (5, 'O Senhor dos Anéis', '9788533613379', 1954, 'EM_MANUTENCAO', 1);

INSERT INTO Livro_Autor (livros_id, autores_id) VALUES (1, 1);
INSERT INTO Livro_Autor (livros_id, autores_id) VALUES (2, 2);
INSERT INTO Livro_Autor (livros_id, autores_id) VALUES (3, 3);
INSERT INTO Livro_Autor (livros_id, autores_id) VALUES (4, 4);
INSERT INTO Livro_Autor (livros_id, autores_id) VALUES (5, 5);

ALTER SEQUENCE Editora_SEQ RESTART WITH 5;
ALTER SEQUENCE DetalhesEditora_SEQ RESTART WITH 5;
ALTER SEQUENCE Autor_SEQ RESTART WITH 6;
ALTER SEQUENCE Livro_SEQ RESTART WITH 6;
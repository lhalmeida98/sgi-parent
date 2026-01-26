package ec.sgi.backend.infrastructure.persistence.adapter;

import ec.sgi.backend.application.port.out.CategoriaRepository;
import ec.sgi.backend.domain.model.Categoria;
import ec.sgi.backend.infrastructure.persistence.entity.CategoriaEntity;
import ec.sgi.backend.infrastructure.persistence.repository.CategoriaJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class CategoriaRepositoryAdapter implements CategoriaRepository {
  private final CategoriaJpaRepository categoriaJpaRepository;

  public CategoriaRepositoryAdapter(CategoriaJpaRepository categoriaJpaRepository) {
    this.categoriaJpaRepository = categoriaJpaRepository;
  }

  @Override
  public Categoria save(Categoria categoria) {
    return toDomain(categoriaJpaRepository.save(toEntity(categoria)));
  }

  @Override
  public List<Categoria> findAll() {
    return categoriaJpaRepository.findAll().stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Categoria> findById(Long id) {
    return categoriaJpaRepository.findById(id).map(this::toDomain);
  }

  @Override
  public List<Categoria> findByEmpresaId(Long empresaId) {
    return categoriaJpaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toDomain)
        .toList();
  }

  @Override
  public Optional<Categoria> findByIdAndEmpresaId(Long id, Long empresaId) {
    return categoriaJpaRepository.findByIdAndEmpresaId(id, empresaId).map(this::toDomain);
  }

  private Categoria toDomain(CategoriaEntity entity) {
    return new Categoria(entity.getId(), entity.getEmpresaId(), entity.getNombre(), entity.getDescripcion());
  }

  private CategoriaEntity toEntity(Categoria categoria) {
    CategoriaEntity entity = new CategoriaEntity();
    entity.setId(categoria.id());
    entity.setEmpresaId(categoria.empresaId());
    entity.setNombre(categoria.nombre());
    entity.setDescripcion(categoria.descripcion());
    return entity;
  }
}

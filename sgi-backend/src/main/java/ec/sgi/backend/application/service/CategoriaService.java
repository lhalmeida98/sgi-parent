package ec.sgi.backend.application.service;

import ec.sgi.backend.application.dto.CategoriaCreateResult;
import ec.sgi.backend.application.dto.CategoriaResult;
import ec.sgi.backend.application.port.in.CrearCategoriaCommand;
import ec.sgi.backend.application.exception.ResourceNotFoundException;
import ec.sgi.backend.application.port.in.ActualizarCategoriaCommand;
import ec.sgi.backend.application.port.in.ActualizarCategoriaUseCase;
import ec.sgi.backend.application.port.in.CrearCategoriaUseCase;
import ec.sgi.backend.application.port.in.ListarCategoriasUseCase;
import ec.sgi.backend.application.port.out.CategoriaRepository;
import ec.sgi.backend.domain.model.Categoria;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategoriaService implements CrearCategoriaUseCase, ListarCategoriasUseCase, ActualizarCategoriaUseCase {
  private final CategoriaRepository categoriaRepository;

  public CategoriaService(CategoriaRepository categoriaRepository) {
    this.categoriaRepository = categoriaRepository;
  }

  @Override
  public CategoriaCreateResult crear(CrearCategoriaCommand command) {
    Categoria categoria = new Categoria(null, command.empresaId(), command.nombre(), command.descripcion());
    Categoria guardada = categoriaRepository.save(categoria);
    return new CategoriaCreateResult(guardada.id());
  }

  @Override
  public List<CategoriaResult> listar(Long empresaId) {
    return categoriaRepository.findByEmpresaId(empresaId).stream()
        .map(this::toResult)
        .toList();
  }

  @Override
  public CategoriaResult actualizar(Long empresaId, Long categoriaId, ActualizarCategoriaCommand command) {
    Categoria existente = categoriaRepository.findByIdAndEmpresaId(categoriaId, empresaId)
        .orElseThrow(() -> new ResourceNotFoundException("Categoria no encontrada"));
    Categoria actualizada = new Categoria(existente.id(), existente.empresaId(), command.nombre(), command.descripcion());
    Categoria guardada = categoriaRepository.save(actualizada);
    return toResult(guardada);
  }

  private CategoriaResult toResult(Categoria categoria) {
    return new CategoriaResult(categoria.id(), categoria.nombre(), categoria.descripcion());
  }
}

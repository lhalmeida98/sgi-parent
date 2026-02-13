package ec.sgi.backend.security;

import java.util.List;

public final class Permisos {
  public static final String FACTURA_GESTION = "FACTURA_GESTION";
  public static final String PREORDEN_GESTION = "PREORDEN_GESTION";
  public static final String CLIENTE_GESTION = "CLIENTE_GESTION";
  public static final String PRODUCTO_GESTION = "PRODUCTO_GESTION";
  public static final String CATEGORIA_GESTION = "CATEGORIA_GESTION";
  public static final String IMPUESTO_GESTION = "IMPUESTO_GESTION";
  public static final String INVENTARIO_GESTION = "INVENTARIO_GESTION";
  public static final String BODEGA_GESTION = "BODEGA_GESTION";
  public static final String PROVEEDOR_GESTION = "PROVEEDOR_GESTION";
  public static final String CXP_GESTION = "CXP_GESTION";
  public static final String PAGO_PROVEEDOR_GESTION = "PAGO_PROVEEDOR_GESTION";
  public static final String USUARIO_ADMIN = "USUARIO_ADMIN";
  public static final String ROL_ADMIN = "ROL_ADMIN";
  public static final String EMPRESA_ADMIN = "EMPRESA_ADMIN";
  public static final List<String> TODOS = List.of(
      FACTURA_GESTION,
      PREORDEN_GESTION,
      CLIENTE_GESTION,
      PRODUCTO_GESTION,
      CATEGORIA_GESTION,
      IMPUESTO_GESTION,
      INVENTARIO_GESTION,
      BODEGA_GESTION,
      PROVEEDOR_GESTION,
      CXP_GESTION,
      PAGO_PROVEEDOR_GESTION,
      USUARIO_ADMIN,
      ROL_ADMIN,
      EMPRESA_ADMIN
  );

  private Permisos() {
  }
}

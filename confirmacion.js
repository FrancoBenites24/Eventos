document.addEventListener('DOMContentLoaded', () => {
  const confirmationContent = document.getElementById('confirmationContent');

  function formatPrice(value) {
    return value === 0 ? 'Gratis' : new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', maximumFractionDigits: 2 }).format(value);
  }

  function getOrderFromURL() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('order');
  }

  function loadOrderConfirmation() {
    const orderId = getOrderFromURL();
    const user = API.me();

    if (!orderId) {
      confirmationContent.innerHTML = `
        <div class="alert alert-danger text-center">
          <i class="bi bi-exclamation-triangle fs-1 mb-3"></i>
          <h4>No se encontró la orden</h4>
          <p>No se pudo encontrar la información de la orden.</p>
          <a href="index.html" class="btn btn-primary">Volver al inicio</a>
        </div>
      `;
      return;
    }

    if (!user) {
      confirmationContent.innerHTML = `
        <div class="alert alert-warning text-center">
          <i class="bi bi-person-circle fs-1 mb-3"></i>
          <h4>Debes iniciar sesión</h4>
          <p>Necesitas iniciar sesión para ver los detalles de tu orden.</p>
          <a href="index.html#login" class="btn btn-primary">Iniciar sesión</a>
        </div>
      `;
      return;
    }

    // Get user's orders
    const orders = API.getOrders(user.id);
    const order = orders.find(o => o.id === orderId);

    if (!order) {
      confirmationContent.innerHTML = `
        <div class="alert alert-danger text-center">
          <i class="bi bi-exclamation-triangle fs-1 mb-3"></i>
          <h4>Orden no encontrada</h4>
          <p>No se pudo encontrar la orden especificada.</p>
          <a href="index.html" class="btn btn-primary">Volver al inicio</a>
        </div>
      `;
      return;
    }

    // Render confirmation
    renderOrderConfirmation(order);
  }

  function renderOrderConfirmation(order) {
    const orderDate = new Date(order.createdAt).toLocaleString('es-PE', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });

    let itemsHtml = '';
    order.items.forEach(item => {
      const event = API.getEvent(item.eventId);
      itemsHtml += `
        <div class="d-flex gap-3 align-items-center border rounded p-3 mb-3">
          <img src="${item.img}" alt="${item.title}" class="rounded" style="width: 80px; height: 80px; object-fit: cover;">
          <div class="flex-grow-1">
            <h6 class="mb-1">${item.title}</h6>
            <div class="small text-muted">
              <i class="bi bi-geo-alt me-1"></i>${item.place || ''} &middot;
              <i class="bi bi-calendar-event ms-2 me-1"></i>${item.when || ''}
            </div>
            <div class="mt-2">
              <span class="badge bg-secondary">${item.qty} ${item.qty === 1 ? 'entrada' : 'entradas'}</span>
              <span class="ms-2 fw-bold">${formatPrice(item.price * item.qty)}</span>
            </div>
          </div>
        </div>
      `;
    });

    let ticketsHtml = '';
    if (order.tickets && order.tickets.length > 0) {
      ticketsHtml = `
        <div class="row g-3">
          ${order.tickets.map(ticket => {
            const event = API.getEvent(ticket.eventId);
            return `
              <div class="col-12 col-md-6 col-lg-4">
                <div class="card h-100 ticket-badge">
                  <div class="card-body text-center">
                    <i class="bi bi-qr-code fs-1 text-primary mb-3"></i>
                    <h6 class="card-title">${event ? event.title : 'Evento'}</h6>
                    <div class="mb-2">
                      <code class="bg-light px-2 py-1 rounded">${ticket.code}</code>
                    </div>
                    <small class="text-muted">Código de entrada válido</small>
                  </div>
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `;
    }

    confirmationContent.innerHTML = `
      <!-- Success Header -->
      <div class="text-center mb-5">
        <div class="success-header rounded-4 p-4 mb-4 mx-auto" style="max-width: 600px;">
          <i class="bi bi-check-circle-fill fs-1 mb-3"></i>
          <h1 class="h2 mb-2">¡Compra Exitosa!</h1>
          <p class="mb-0">Tu orden ha sido procesada correctamente</p>
        </div>
      </div>

      <div class="row g-4">
        <!-- Order Details -->
        <div class="col-lg-8">
          <div class="card confirmation-card">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="bi bi-receipt me-2"></i>
                Detalles de la Orden
              </h5>
            </div>
            <div class="card-body">
              <div class="row g-3 mb-4">
                <div class="col-md-6">
                  <div class="border rounded p-3">
                    <div class="small text-muted mb-1">Número de Orden</div>
                    <div class="fw-bold fs-5">${order.id}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded p-3">
                    <div class="small text-muted mb-1">Fecha de Compra</div>
                    <div class="fw-bold">${orderDate}</div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded p-3">
                    <div class="small text-muted mb-1">Método de Pago</div>
                    <div class="fw-bold">
                      <i class="bi ${getPaymentIcon(order.payMethod)} me-1"></i>
                      ${getPaymentMethodName(order.payMethod)}
                    </div>
                  </div>
                </div>
                <div class="col-md-6">
                  <div class="border rounded p-3">
                    <div class="small text-muted mb-1">Estado</div>
                    <div class="fw-bold text-success">
                      <i class="bi bi-check-circle me-1"></i>
                      Confirmada
                    </div>
                  </div>
                </div>
              </div>

              <h6 class="mb-3">Artículos Comprados</h6>
              ${itemsHtml}
            </div>
          </div>
        </div>

        <!-- Summary & Actions -->
        <div class="col-lg-4">
          <div class="card confirmation-card mb-4">
            <div class="card-header bg-light">
              <h6 class="mb-0">Resumen de Pago</h6>
            </div>
            <div class="card-body">
              <div class="d-flex justify-content-between mb-2">
                <span>Subtotal</span>
                <span>${formatPrice(order.subtotal)}</span>
              </div>
              ${order.qtyDiscount > 0 ? `
                <div class="d-flex justify-content-between mb-2 text-success">
                  <span>Descuento por cantidad</span>
                  <span>-${formatPrice(order.qtyDiscount)}</span>
                </div>
              ` : ''}
              ${order.coupon ? `
                <div class="d-flex justify-content-between mb-2 text-success">
                  <span>Cupón (${order.coupon})</span>
                  <span>-${formatPrice(order.couponDiscount || 0)}</span>
                </div>
              ` : ''}
              <hr>
              <div class="d-flex justify-content-between fw-bold fs-5">
                <span>Total Pagado</span>
                <span>${formatPrice(order.total)}</span>
              </div>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="card">
            <div class="card-body">
              <h6 class="mb-3">¿Qué deseas hacer ahora?</h6>
              <div class="d-grid gap-2">
                <a href="index.html" class="btn btn-primary">
                  <i class="bi bi-house me-2"></i>
                  Seguir Comprando
                </a>
                <a href="profile.html" class="btn btn-outline-primary">
                  <i class="bi bi-person me-2"></i>
                  Ver Mi Perfil
                </a>
                <button class="btn btn-outline-secondary" onclick="window.print()">
                  <i class="bi bi-printer me-2"></i>
                  Imprimir Confirmación
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Tickets Section -->
      ${ticketsHtml ? `
        <div class="mt-5">
          <div class="card confirmation-card">
            <div class="card-header bg-light">
              <h5 class="mb-0">
                <i class="bi bi-ticket-perforated me-2"></i>
                Tus Entradas Digitales
              </h5>
            </div>
            <div class="card-body">
              <p class="text-muted mb-4">
                Guarda estos códigos QR o imprímelos. Son tu entrada para los eventos.
              </p>
              ${ticketsHtml}
            </div>
          </div>
        </div>
      ` : ''}

      <!-- Additional Info -->
      <div class="mt-4">
        <div class="alert alert-info">
          <i class="bi bi-info-circle me-2"></i>
          <strong>Información importante:</strong>
          <ul class="mb-0 mt-2">
            <li>Recibirás un email de confirmación con los detalles de tu compra</li>
            <li>Presenta los códigos QR en la entrada del evento</li>
            <li>Los tickets son personales e intransferibles</li>
            <li>Para cambios o cancelaciones, contacta con el organizador</li>
          </ul>
        </div>
      </div>
    `;
  }

  function getPaymentIcon(method) {
    switch (method) {
      case 'TARJETA': return 'bi-credit-card';
      case 'YAPE': return 'bi-phone';
      case 'PLIN': return 'bi-cash';
      case 'BILLETERA': return 'bi-wallet2';
      default: return 'bi-cash';
    }
  }

  function getPaymentMethodName(method) {
    switch (method) {
      case 'TARJETA': return 'Tarjeta de Crédito/Débito';
      case 'YAPE': return 'Yape';
      case 'PLIN': return 'Plin';
      case 'BILLETERA': return 'Billetera Virtual';
      default: return method;
    }
  }

  // Load confirmation on page load
  loadOrderConfirmation();
});

document.addEventListener('DOMContentLoaded', () => {
  const checkoutContent = document.getElementById('checkoutContent');

  function formatPrice(value) {
    return value === 0 ? 'Gratis' : new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN', maximumFractionDigits: 2 }).format(value);
  }

  function renderCheckout(cartItems) {
    if (!cartItems || cartItems.length === 0) {
      checkoutContent.innerHTML = '<p>Tu carrito está vacío.</p>';
      return;
    }

    let html = `
      <div class="row g-4">
        <div class="col-lg-8">
          <div id="cartItemsList" class="list-group">
    `;

    cartItems.forEach(item => {
      html += `
        <div class="list-group-item d-flex gap-3 py-3" aria-label="${item.title}">
          <img src="${item.img}" alt="${item.title}" class="rounded" style="width: 96px; height: 96px; object-fit: cover;">
          <div class="flex-grow-1">
            <div class="d-flex justify-content-between align-items-start">
              <div>
                <h5 class="mb-1">${item.title}</h5>
                <div class="small text-muted">
                  <i class="bi bi-geo-alt me-1"></i>${item.place || ''} &middot; <i class="bi bi-calendar-event ms-2 me-1"></i>${item.when || ''}
                </div>
              </div>
              <button class="btn btn-sm btn-outline-danger btn-remove-item" data-id="${item.id}" aria-label="Quitar ${item.title} del carrito">
                <i class="bi bi-x"></i>
              </button>
            </div>
            <div class="d-flex justify-content-between align-items-center mt-2">
              <div class="input-group input-group-sm" style="width: 120px;">
                <button class="btn btn-outline-secondary btn-qty-minus" data-id="${item.id}" type="button" aria-label="Disminuir cantidad">-</button>
                <input type="number" class="form-control text-center qty-input" min="1" value="${item.qty}" data-id="${item.id}" aria-label="Cantidad de ${item.title}">
                <button class="btn btn-outline-secondary btn-qty-plus" data-id="${item.id}" type="button" aria-label="Aumentar cantidad">+</button>
              </div>
              <strong>${formatPrice(item.price * item.qty)}</strong>
            </div>
          </div>
        </div>
      `;
    });

    html += `
          </div>
        </div>
        <div class="col-lg-4">
          <div class="card p-3">
            <h4>Resumen</h4>
            <div class="d-flex justify-content-between">
              <span>Subtotal</span>
              <span id="subtotalAmount">S/ 0.00</span>
            </div>
            <div class="d-flex justify-content-between small text-muted">
              <span>Desc. por cantidad</span>
              <span id="qtyDiscountAmount">S/ 0.00</span>
            </div>
            <div class="d-flex justify-content-between small text-muted">
              <span>Cupones</span>
              <span id="couponDiscountAmount">S/ 0.00</span>
            </div>
            <hr>
            <div class="d-flex justify-content-between fw-bold fs-5">
              <span>Total</span>
              <span id="totalAmount">S/ 0.00</span>
            </div>
            <button id="btnCompletePurchase" class="btn btn-primary w-100 mt-3">Finalizar compra</button>
          </div>
        </div>
      </div>
    `;

    checkoutContent.innerHTML = html;

    // Update totals display
    updateTotals();

    // Add event listeners for quantity controls and remove buttons
    document.querySelectorAll('.btn-remove-item').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        API.removeCartItem(id);
        refreshCart();
      });
    });

    document.querySelectorAll('.btn-qty-minus').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        const cart = API.getCart();
        const item = cart.find(i => i.id === id);
        if (item && item.qty > 1) {
          API.updateCartItem(id, item.qty - 1);
          refreshCart();
        }
      });
    });

    document.querySelectorAll('.btn-qty-plus').forEach(btn => {
      btn.addEventListener('click', () => {
        const id = btn.dataset.id;
        const cart = API.getCart();
        const item = cart.find(i => i.id === id);
        if (item) {
          API.updateCartItem(id, item.qty + 1);
          refreshCart();
        }
      });
    });

    document.querySelectorAll('.qty-input').forEach(input => {
      input.addEventListener('change', () => {
        const id = input.dataset.id;
        let val = parseInt(input.value);
        if (isNaN(val) || val < 1) val = 1;
        API.updateCartItem(id, val);
        refreshCart();
      });
    });

    // Finalize purchase button
    document.getElementById('btnCompletePurchase').addEventListener('click', () => {
      showPaymentModal();
    });
  }

  function updateTotals() {
    const cart = API.getCart();
    const totals = API.calcTotals(cart);

    document.getElementById('subtotalAmount').textContent = formatPrice(totals.subtotal);
    document.getElementById('qtyDiscountAmount').textContent = `- ${formatPrice(totals.qtyDiscount)}`;
    document.getElementById('couponDiscountAmount').textContent = `- ${formatPrice(totals.couponDiscount)}`;
    document.getElementById('totalAmount').textContent = formatPrice(totals.total);
  }

  function refreshCart() {
    const cart = API.getCart();
    renderCheckout(cart);
  }

  function showPaymentModal() {
    const cart = API.getCart();
    if (!cart || cart.length === 0) {
      alert('Tu carrito está vacío');
      return;
    }

    const totals = API.calcTotals(cart);
    const user = API.me();

    if (!user) {
      alert('Debes iniciar sesión para realizar una compra');
      window.location.href = 'index.html#login';
      return;
    }

    const paymentOptions = document.getElementById('paymentOptions');
    paymentOptions.innerHTML = `
      <div class="alert alert-info">
        <i class="bi bi-info-circle me-2"></i>
        Total a pagar: <strong>${formatPrice(totals.total)}</strong>
      </div>

      <h5 class="mb-3">Selecciona tu método de pago:</h5>

      <div class="row g-3">
        <!-- Tarjeta de Crédito -->
        <div class="col-md-6">
          <div class="card payment-option" data-method="TARJETA">
            <div class="card-body text-center">
              <i class="bi bi-credit-card fs-1 text-primary mb-2"></i>
              <h6 class="card-title">Tarjeta de Crédito/Débito</h6>
              <p class="card-text small text-muted">Visa, Mastercard, American Express</p>
              <button class="btn btn-outline-primary w-100 select-payment" data-method="TARJETA">
                Seleccionar
              </button>
            </div>
          </div>
        </div>

        <!-- Yape -->
        <div class="col-md-6">
          <div class="card payment-option" data-method="YAPE">
            <div class="card-body text-center">
              <i class="bi bi-phone fs-1 text-success mb-2"></i>
              <h6 class="card-title">Yape</h6>
              <p class="card-text small text-muted">Pago móvil rápido y seguro</p>
              <button class="btn btn-outline-success w-100 select-payment" data-method="YAPE">
                Seleccionar
              </button>
            </div>
          </div>
        </div>

        <!-- Plin -->
        <div class="col-md-6">
          <div class="card payment-option" data-method="PLIN">
            <div class="card-body text-center">
              <i class="bi bi-cash fs-1 text-warning mb-2"></i>
              <h6 class="card-title">Plin</h6>
              <p class="card-text small text-muted">Transferencia instantánea</p>
              <button class="btn btn-outline-warning w-100 select-payment" data-method="PLIN">
                Seleccionar
              </button>
            </div>
          </div>
        </div>

        <!-- Billetera -->
        <div class="col-md-6">
          <div class="card payment-option" data-method="BILLETERA">
            <div class="card-body text-center">
              <i class="bi bi-wallet2 fs-1 text-info mb-2"></i>
              <h6 class="card-title">Billetera Virtual</h6>
              <p class="card-text small text-muted">Saldo disponible: ${formatPrice(user.wallet || 0)}</p>
              ${((user.wallet || 0) < totals.total) ?
                '<div class="alert alert-warning small">Saldo insuficiente</div>' :
                '<button class="btn btn-outline-info w-100 select-payment" data-method="BILLETERA">Seleccionar</button>'}
            </div>
          </div>
        </div>
      </div>

      <div class="mt-4 d-flex gap-2">
        <button class="btn btn-secondary" data-bs-dismiss="modal">Cancelar</button>
        <div id="paymentButtons" class="d-none">
          <button id="btnConfirmPayment" class="btn btn-success">Confirmar Pago</button>
        </div>
      </div>
    `;

    // Add event listeners for payment selection
    paymentOptions.querySelectorAll('.select-payment').forEach(btn => {
      btn.addEventListener('click', (e) => {
        const method = e.target.dataset.method;

        // Remove active class from all options
        paymentOptions.querySelectorAll('.payment-option').forEach(opt => {
          opt.classList.remove('border-primary');
        });

        // Add active class to selected option
        e.target.closest('.payment-option').classList.add('border-primary');

        // Show confirm button
        document.getElementById('paymentButtons').classList.remove('d-none');

        // Store selected method
        selectedPaymentMethod = method;
      });
    });

    // Confirm payment
    document.getElementById('btnConfirmPayment').addEventListener('click', () => {
      processPayment(selectedPaymentMethod);
    });

    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('paymentModal'));
    modal.show();
  }

  function processPayment(method) {
    const cart = API.getCart();
    const totals = API.calcTotals(cart);
    const user = API.me();

    try {
      // Create order
      const order = API.createOrder({
        userId: user.id,
        items: cart,
        payMethod: method
      });

      // Handle wallet payment
      if (method === 'BILLETERA') {
        const newBalance = API.addWallet(-totals.total);
        if (newBalance === null) {
          throw new Error('Error al actualizar el saldo de la billetera');
        }
      }

      // Clear cart
      API.clearCart();

      // Close modal
      const modal = bootstrap.Modal.getInstance(document.getElementById('paymentModal'));
      if (modal) {
        modal.hide();
      }

      // Show success message and redirect
      setTimeout(() => {
        alert(`¡Pago realizado con éxito!\nOrden: ${order.id}\nMétodo: ${method}`);
        window.location.href = `confirmacion.html?order=${order.id}`;
      }, 500);

    } catch (error) {
      console.error('Error processing payment:', error);
      alert('Error al procesar el pago. Por favor intenta nuevamente.');
    }
  }

  let selectedPaymentMethod = null;

  // Initial render
  refreshCart();
});

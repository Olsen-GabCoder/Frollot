// screen-booking.jsx — Réservation (étape Date & heure)
function BookingScreen() {
  const steps = ['Service', 'Date', 'Coiffeur', 'Récap'];
  const days = [
    { d: 'LUN', n: 2, off: false }, { d: 'MAR', n: 3, off: false }, { d: 'MER', n: 4, sel: true },
    { d: 'JEU', n: 5, off: false }, { d: 'VEN', n: 6, off: true }, { d: 'SAM', n: 7, off: false }, { d: 'DIM', n: 8, off: true },
  ];
  const slots = ['09:00', '09:45', '10:30', '11:15', '14:00', '14:45', '15:30', '16:15', '17:00'];
  const selSlot = '14:45';
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: 'var(--background)' }}>
      {/* Header */}
      <div style={{ background: 'var(--surface)', padding: '6px 8px 16px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 6, minHeight: 52 }}>
          <button style={iconBtn}><Icon name="arrow_back" /></button>
          <div style={{ flex: 1 }}>
            <div className="t-title-lg" style={{ color: 'var(--on-surface)', lineHeight: 1.1 }}>Réserver</div>
            <div className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>Salon Lumière</div>
          </div>
          <button style={iconBtn}><Icon name="close" /></button>
        </div>
        {/* Stepper */}
        <div style={{ display: 'flex', alignItems: 'center', padding: '8px 6px 0' }}>
          {steps.map((s, i) => {
            const done = i < 1, active = i === 1;
            return (
              <React.Fragment key={i}>
                <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 5 }}>
                  <div style={{
                    width: 28, height: 28, borderRadius: '50%', display: 'grid', placeItems: 'center',
                    background: done ? 'var(--success)' : active ? 'var(--primary)' : 'var(--surface-container-high)',
                    color: (done || active) ? '#fff' : 'var(--on-surface-variant)',
                    border: active ? 'none' : done ? 'none' : '1px solid var(--outline-variant)',
                    fontFamily: 'var(--font-sans)', fontWeight: 700, fontSize: 13,
                  }}>
                    {done ? <Icon name="check" size={16} color="#fff" /> : i + 1}
                  </div>
                  <span className="t-label-md" style={{ color: active ? 'var(--primary)' : 'var(--on-surface-variant)', textTransform: 'none', letterSpacing: 0, fontSize: 11 }}>{s}</span>
                </div>
                {i < steps.length - 1 && <div style={{ flex: 1, height: 2, background: done ? 'var(--success)' : 'var(--outline-variant)', margin: '0 4px', marginBottom: 18 }} />}
              </React.Fragment>
            );
          })}
        </div>
      </div>

      {/* Contenu */}
      <div style={{ flex: 1, overflow: 'auto', padding: '20px 20px 16px' }}>
        {/* Service sélectionné */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px', borderRadius: 'var(--radius-md)', background: 'var(--primary-container)', marginBottom: 22 }}>
          <Icon name="content_cut" size={22} color="var(--on-primary-container)" />
          <div style={{ flex: 1 }}>
            <div className="t-title-sm" style={{ color: 'var(--on-primary-container)' }}>Balayage + patine</div>
            <div className="t-body-sm" style={{ color: 'var(--on-primary-container)', opacity: .8 }}>2 h 30 · 140 €</div>
          </div>
          <span className="t-label-lg" style={{ color: 'var(--primary)' }}>Modifier</span>
        </div>

        {/* Mois */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 14 }}>
          <h3 style={{ fontFamily: 'var(--font-display)', fontSize: 22, fontWeight: 600, color: 'var(--on-surface)' }}>Juin 2026</h3>
          <div style={{ display: 'flex', gap: 4 }}>
            <button style={{ ...iconBtn, width: 36, height: 36 }}><Icon name="chevron_left" size={22} /></button>
            <button style={{ ...iconBtn, width: 36, height: 36 }}><Icon name="chevron_right" size={22} /></button>
          </div>
        </div>

        {/* Jours */}
        <div style={{ display: 'flex', gap: 8, marginBottom: 26 }}>
          {days.map((day, i) => (
            <div key={i} style={{
              flex: 1, textAlign: 'center', padding: '10px 0 12px', borderRadius: 'var(--radius-md)',
              background: day.sel ? 'var(--primary)' : day.off ? 'transparent' : 'var(--surface)',
              border: day.sel ? 'none' : day.off ? '1px dashed var(--outline-variant)' : '1px solid var(--outline-variant)',
              opacity: day.off ? .5 : 1,
            }}>
              <div style={{ fontFamily: 'var(--font-sans)', fontSize: 10.5, fontWeight: 700, letterSpacing: .5, color: day.sel ? 'rgba(255,255,255,.8)' : 'var(--on-surface-variant)' }}>{day.d}</div>
              <div className="t-title-md" style={{ color: day.sel ? '#fff' : 'var(--on-surface)', marginTop: 3 }}>{day.n}</div>
            </div>
          ))}
        </div>

        {/* Créneaux */}
        <h4 className="t-overline" style={{ color: 'var(--on-surface-variant)', marginBottom: 12 }}>Créneaux disponibles · Mercredi 4</h4>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: 10 }}>
          {slots.map((t, i) => {
            const sel = t === selSlot;
            return (
              <div key={i} style={{
                textAlign: 'center', padding: '13px 0', borderRadius: 'var(--radius-sm)',
                fontFamily: 'var(--font-sans)', fontWeight: 700, fontSize: 15,
                background: sel ? 'var(--primary)' : 'var(--surface)',
                color: sel ? '#fff' : 'var(--on-surface)',
                border: sel ? 'none' : '1px solid var(--outline-variant)',
              }}>{t}</div>
            );
          })}
        </div>
      </div>

      {/* Récap bas */}
      <div style={{ background: 'var(--surface)', borderTop: '1px solid var(--outline-variant)', padding: '14px 20px 18px', display: 'flex', alignItems: 'center', gap: 14 }}>
        <div>
          <div className="t-body-sm" style={{ color: 'var(--on-surface-variant)' }}>Mer. 4 juin · 14:45</div>
          <div className="t-title-md" style={{ color: 'var(--on-surface)' }}>140 €</div>
        </div>
        <Btn kind="primary" full icon="arrow_forward" style={{ boxShadow: 'var(--elev-2)' }}>Continuer</Btn>
      </div>
    </div>
  );
}

Object.assign(window, { BookingScreen });

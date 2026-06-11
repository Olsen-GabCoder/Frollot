// screen-login.jsx — Écran de connexion Frollot
function LoginScreen() {
  return (
    <div style={{ height: '100%', display: 'flex', flexDirection: 'column', background: 'var(--surface)', position: 'relative' }}>
      {/* Hero éditorial */}
      <div style={{ position: 'relative', height: 320, flexShrink: 0 }}>
        <Ph label="ambiance salon · plein cadre" h={320} r={0} tone="primary" />
        <div style={{
          position: 'absolute', inset: 0,
          background: 'linear-gradient(180deg, rgba(40,23,51,.42) 0%, rgba(40,23,51,.08) 45%, rgba(251,247,249,.0) 70%, var(--surface) 100%)',
        }} />
        {/* Marque */}
        <div style={{ position: 'absolute', top: 28, left: 24, display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 48, height: 48, borderRadius: 'var(--radius-md)',
            background: 'rgba(255,255,255,.16)', backdropFilter: 'blur(6px)',
            border: '1px solid rgba(255,255,255,.35)',
            display: 'grid', placeItems: 'center',
            fontFamily: 'var(--font-display)', fontWeight: 600, fontSize: 32, color: '#fff',
          }}>F</div>
          <div style={{ fontFamily: 'var(--font-display)', fontSize: 26, fontWeight: 600, color: '#fff', letterSpacing: .5 }}>Frollot</div>
        </div>
      </div>

      {/* Formulaire */}
      <div style={{ flex: 1, padding: '4px 24px 24px', display: 'flex', flexDirection: 'column', overflow: 'auto' }}>
        <div className="t-overline" style={{ color: 'var(--secondary)', marginBottom: 6 }}>Bon retour parmi nous</div>
        <h1 style={{ fontFamily: 'var(--font-display)', fontSize: 44, fontWeight: 600, lineHeight: .98, letterSpacing: '-.5px', color: 'var(--on-surface)' }}>Bienvenue</h1>
        <p className="t-body-md" style={{ color: 'var(--on-surface-variant)', marginTop: 10 }}>Connectez-vous pour retrouver vos salons, rendez-vous et inspirations.</p>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 14, marginTop: 24 }}>
          <Field icon="mail" label="Email" value="camille.r@email.com" />
          <Field icon="lock" label="Mot de passe" value="••••••••••" trailing="visibility_off" focused />
        </div>

        <button style={{
          alignSelf: 'flex-end', marginTop: 12, background: 'none', border: 'none', cursor: 'pointer',
          color: 'var(--primary)', fontFamily: 'var(--font-sans)', fontWeight: 700, fontSize: 13.5, padding: '4px 2px',
        }}>Mot de passe oublié ?</button>

        <Btn kind="primary" full icon="login" style={{ marginTop: 14, boxShadow: 'var(--elev-2)' }}>Se connecter</Btn>

        {/* Séparateur */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, margin: '22px 0' }}>
          <div style={{ flex: 1, height: 1, background: 'var(--outline-variant)' }} />
          <span className="t-label-md" style={{ color: 'var(--on-surface-variant)', letterSpacing: 1 }}>OU</span>
          <div style={{ flex: 1, height: 1, background: 'var(--outline-variant)' }} />
        </div>

        <Btn kind="outline" full>Créer un compte</Btn>

        <p className="t-body-sm" style={{ color: 'var(--on-surface-variant)', textAlign: 'center', marginTop: 'auto', paddingTop: 20, lineHeight: 1.5 }}>
          En continuant, vous acceptez nos <span style={{ color: 'var(--primary)', fontWeight: 600 }}>Conditions</span> et notre <span style={{ color: 'var(--primary)', fontWeight: 600 }}>Politique de confidentialité</span>.
        </p>
      </div>
    </div>
  );
}

// Champ de saisie pour l'auth
function Field({ icon, label, value, trailing, focused = false }) {
  return (
    <div>
      <div className="t-label-md" style={{ color: focused ? 'var(--primary)' : 'var(--on-surface-variant)', marginBottom: 6, letterSpacing: .3, textTransform: 'none', fontSize: 12.5 }}>{label}</div>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 12, height: 56, padding: '0 16px',
        borderRadius: 'var(--radius-sm)', background: 'var(--surface)',
        border: `${focused ? 2 : 1}px solid ${focused ? 'var(--primary)' : 'var(--outline)'}`,
      }}>
        <Icon name={icon} size={22} color={focused ? 'var(--primary)' : 'var(--on-surface-variant)'} />
        <span className="t-body-lg" style={{ color: 'var(--on-surface)', flex: 1 }}>{value}</span>
        {trailing && <Icon name={trailing} size={22} color="var(--on-surface-variant)" />}
      </div>
    </div>
  );
}

Object.assign(window, { LoginScreen });
